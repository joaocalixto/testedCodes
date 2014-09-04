/*      */ package net.sourceforge.jtds.jdbc;
/*      */ 
/*      */ import java.io.DataInputStream;
/*      */ import java.io.DataOutputStream;
/*      */ import java.io.EOFException;
/*      */ import java.io.File;
/*      */ import java.io.IOException;
/*      */ import java.io.RandomAccessFile;
/*      */ import java.lang.reflect.Constructor;
/*      */ import java.lang.reflect.InvocationTargetException;
/*      */ import java.lang.reflect.Method;
/*      */ import java.net.Socket;
/*      */ import java.net.SocketException;
/*      */ import java.net.UnknownHostException;
/*      */ import java.util.ArrayList;
/*      */ import java.util.LinkedList;
/*      */ import javax.net.SocketFactory;
/*      */ import net.sourceforge.jtds.ssl.SocketFactories;
/*      */ import net.sourceforge.jtds.ssl.SocketFactoriesSUN;
/*      */ import net.sourceforge.jtds.util.Logger;
/*      */ 
/*      */ class SharedSocket
/*      */ {
/*      */   private Socket socket;
/*      */   private Socket sslSocket;
/*      */   private DataOutputStream out;
/*      */   private DataInputStream in;
/*      */   private int maxBufSize;
/*      */   private final ArrayList socketTable;
/*      */   private int responseOwner;
/*      */   private final byte[] hdrBuf;
/*      */   private final File bufferDir;
/*      */   private static int globalMemUsage;
/*      */   private static int peakMemUsage;
/*  177 */   private static int memoryBudget = 100000;
/*      */ 
/*  183 */   private static int minMemPkts = 8;
/*      */   private static boolean securityViolation;
/*      */   private int tdsVersion;
/*      */   protected final int serverType;
/*      */   private CharsetInfo charsetInfo;
/*      */   private int packetCount;
/*      */   private String host;
/*      */   private int port;
/*      */   private boolean cancelPending;
/*      */   private Object cancelMonitor;
/*      */   private byte[] doneBuffer;
/*      */   private int doneBufferFrag;
/*      */   private static final int TDS_DONE_TOKEN = 253;
/*      */   private static final int TDS_DONE_LEN = 9;
/*      */   private static final int TDS_HDR_LEN = 8;
/*      */ 
/*      */   protected SharedSocket(File bufferDir, int tdsVersion, int serverType)
/*      */   {
/*  144 */     this.maxBufSize = 512;
/*      */ 
/*  148 */     this.socketTable = new ArrayList();
/*      */ 
/*  152 */     this.responseOwner = -1;
/*      */ 
/*  156 */     this.hdrBuf = new byte[8];
/*      */ 
/*  221 */     this.cancelMonitor = new Object();
/*      */ 
/*  225 */     this.doneBuffer = new byte[9];
/*      */ 
/*  229 */     this.doneBufferFrag = 0;
/*      */ 
/*  244 */     this.bufferDir = bufferDir;
/*  245 */     this.tdsVersion = tdsVersion;
/*  246 */     this.serverType = serverType;
/*      */   }
/*      */ 
/*      */   SharedSocket(ConnectionJDBC2 connection)
/*      */     throws IOException, UnknownHostException
/*      */   {
/*  257 */     this(connection.getBufferDir(), connection.getTdsVersion(), connection.getServerType());
/*  258 */     this.host = connection.getServerName();
/*  259 */     this.port = connection.getPortNumber();
/*  260 */     if (Driver.JDBC3)
/*  261 */       this.socket = createSocketForJDBC3(connection);
/*      */     else {
/*  263 */       this.socket = new Socket(this.host, this.port);
/*      */     }
/*  265 */     setOut(new DataOutputStream(this.socket.getOutputStream()));
/*  266 */     setIn(new DataInputStream(this.socket.getInputStream()));
/*  267 */     this.socket.setTcpNoDelay(connection.getTcpNoDelay());
/*  268 */     this.socket.setSoTimeout(connection.getSocketTimeout() * 1000);
/*  269 */     this.socket.setKeepAlive(connection.getSocketKeepAlive());
/*      */   }
/*      */ 
/*      */   private Socket createSocketForJDBC3(ConnectionJDBC2 connection)
/*      */     throws IOException
/*      */   {
/*  282 */     String host = connection.getServerName();
/*  283 */     int port = connection.getPortNumber();
/*  284 */     int loginTimeout = connection.getLoginTimeout();
/*  285 */     String bindAddress = connection.getBindAddress();
/*      */     try
/*      */     {
/*  288 */       Constructor socketConstructor = Socket.class.getConstructor(new Class[0]);
/*      */ 
/*  290 */       Socket socket = (Socket)socketConstructor.newInstance(new Object[0]);
/*      */ 
/*  294 */       Constructor constructor = Class.forName("java.net.InetSocketAddress").getConstructor(new Class[] { String.class, Integer.TYPE });
/*      */ 
/*  296 */       Object address = constructor.newInstance(new Object[] { host, new Integer(port) });
/*      */ 
/*  300 */       if ((bindAddress != null) && (bindAddress.length() > 0)) {
/*  301 */         Object localBindAddress = constructor.newInstance(new Object[] { bindAddress, new Integer(0) });
/*      */ 
/*  303 */         Method bind = class$java$net$Socket.getMethod("bind", new Class[] { Class.forName("java.net.SocketAddress") });
/*      */ 
/*  305 */         bind.invoke(socket, new Object[] { localBindAddress });
/*      */       }
/*      */ 
/*  309 */       Method connect = class$java$net$Socket.getMethod("connect", new Class[] { Class.forName("java.net.SocketAddress"), Integer.TYPE });
/*      */ 
/*  311 */       connect.invoke(socket, new Object[] { address, new Integer(loginTimeout * 1000) });
/*      */ 
/*  314 */       return socket;
/*      */     }
/*      */     catch (InvocationTargetException ite)
/*      */     {
/*  318 */       Throwable cause = ite.getTargetException();
/*  319 */       if (cause instanceof IOException)
/*      */       {
/*  321 */         throw ((IOException)cause);
/*      */       }
/*      */ 
/*  325 */       throw ((IOException)Support.linkException(new IOException("Could not create socket"), cause));
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*      */     }
/*  330 */     return new Socket(host, port);
/*      */   }
/*      */ 
/*      */   void enableEncryption(String ssl)
/*      */     throws IOException
/*      */   {
/*  342 */     Logger.println("Enabling TLS encryption");
/*  343 */     SocketFactory sf = (Driver.JDBC3) ? SocketFactories.getSocketFactory(ssl, this.socket) : SocketFactoriesSUN.getSocketFactory(ssl, this.socket);
/*      */ 
/*  346 */     this.sslSocket = sf.createSocket(getHost(), getPort());
/*  347 */     setOut(new DataOutputStream(this.sslSocket.getOutputStream()));
/*  348 */     setIn(new DataInputStream(this.sslSocket.getInputStream()));
/*      */   }
/*      */ 
/*      */   void disableEncryption()
/*      */     throws IOException
/*      */   {
/*  357 */     Logger.println("Disabling TLS encryption");
/*  358 */     this.sslSocket.close();
/*  359 */     this.sslSocket = null;
/*  360 */     setOut(new DataOutputStream(this.socket.getOutputStream()));
/*  361 */     setIn(new DataInputStream(this.socket.getInputStream()));
/*      */   }
/*      */ 
/*      */   void setCharsetInfo(CharsetInfo charsetInfo)
/*      */   {
/*  371 */     this.charsetInfo = charsetInfo;
/*      */   }
/*      */ 
/*      */   CharsetInfo getCharsetInfo()
/*      */   {
/*  379 */     return this.charsetInfo;
/*      */   }
/*      */ 
/*      */   String getCharset()
/*      */   {
/*  389 */     return this.charsetInfo.getCharset();
/*      */   }
/*      */ 
/*      */   RequestStream getRequestStream(int bufferSize, int maxPrecision)
/*      */   {
/*  401 */     synchronized (this.socketTable)
/*      */     {
/*  403 */       for (int id = 0; id < this.socketTable.size(); ++id) {
/*  404 */         if (this.socketTable.get(id) == null) {
/*      */           break;
/*      */         }
/*      */       }
/*      */ 
/*  409 */       VirtualSocket vsock = new VirtualSocket(id);
/*      */ 
/*  411 */       if (id >= this.socketTable.size())
/*  412 */         this.socketTable.add(vsock);
/*      */       else {
/*  414 */         this.socketTable.set(id, vsock);
/*      */       }
/*      */ 
/*  417 */       return new RequestStream(this, id, bufferSize, maxPrecision);
/*      */     }
/*      */   }
/*      */ 
/*      */   ResponseStream getResponseStream(RequestStream requestStream, int bufferSize)
/*      */   {
/*  433 */     return new ResponseStream(this, requestStream.getStreamId(), bufferSize);
/*      */   }
/*      */ 
/*      */   int getTdsVersion()
/*      */   {
/*  443 */     return this.tdsVersion;
/*      */   }
/*      */ 
/*      */   protected void setTdsVersion(int tdsVersion)
/*      */   {
/*  452 */     this.tdsVersion = tdsVersion;
/*      */   }
/*      */ 
/*      */   static void setMemoryBudget(int memoryBudget)
/*      */   {
/*  461 */     memoryBudget = memoryBudget;
/*      */   }
/*      */ 
/*      */   static int getMemoryBudget()
/*      */   {
/*  470 */     return memoryBudget;
/*      */   }
/*      */ 
/*      */   static void setMinMemPkts(int minMemPkts)
/*      */   {
/*  480 */     minMemPkts = minMemPkts;
/*      */   }
/*      */ 
/*      */   static int getMinMemPkts()
/*      */   {
/*  489 */     return minMemPkts;
/*      */   }
/*      */ 
/*      */   boolean isConnected()
/*      */   {
/*  498 */     return (this.socket != null);
/*      */   }
/*      */ 
/*      */   boolean cancel(int streamId)
/*      */   {
/*  513 */     synchronized (this.cancelMonitor)
/*      */     {
/*  521 */       if ((this.responseOwner != streamId) || (this.cancelPending));
/*      */     }
/*      */ 
/*  548 */     label132: return false;
/*      */   }
/*      */ 
/*      */   void close()
/*      */     throws IOException
/*      */   {
/*  557 */     if (Logger.isActive()) {
/*  558 */       Logger.println("TdsSocket: Max buffer memory used = " + (peakMemUsage / 1024) + "KB");
/*      */     }
/*      */ 
/*  561 */     synchronized (this.socketTable)
/*      */     {
/*  563 */       for (int i = 0; i < this.socketTable.size(); ++i) {
/*  564 */         VirtualSocket vsock = (VirtualSocket)this.socketTable.get(i);
/*      */ 
/*  566 */         if ((vsock == null) || (vsock.diskQueue == null)) continue;
/*      */         try {
/*  568 */           vsock.diskQueue.close();
/*  569 */           vsock.queueFile.delete();
/*      */         }
/*      */         catch (IOException ioe)
/*      */         {
/*      */         }
/*      */       }
/*      */       try {
/*  576 */         if (this.sslSocket != null) {
/*  577 */           this.sslSocket.close();
/*  578 */           this.sslSocket = null;
/*      */         }
/*      */       }
/*      */       finally {
/*  582 */         if (this.socket != null)
/*  583 */           this.socket.close();
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   void forceClose()
/*      */   {
/*  595 */     if (this.socket == null) return;
/*      */     try {
/*  597 */       this.socket.close();
/*      */     } catch (IOException ioe) {
/*      */     }
/*      */     finally {
/*  601 */       this.sslSocket = null;
/*  602 */       this.socket = null;
/*      */     }
/*      */   }
/*      */ 
/*      */   void closeStream(int streamId)
/*      */   {
/*  613 */     synchronized (this.socketTable) {
/*  614 */       VirtualSocket vsock = lookup(streamId);
/*      */ 
/*  616 */       if (vsock.diskQueue != null) {
/*      */         try {
/*  618 */           vsock.diskQueue.close();
/*  619 */           vsock.queueFile.delete();
/*      */         }
/*      */         catch (IOException ioe)
/*      */         {
/*      */         }
/*      */       }
/*  625 */       this.socketTable.set(streamId, null);
/*      */     }
/*      */   }
/*      */ 
/*      */   byte[] sendNetPacket(int streamId, byte[] buffer)
/*      */     throws IOException
/*      */   {
/*  641 */     synchronized (this.socketTable)
/*      */     {
/*  643 */       VirtualSocket vsock = lookup(streamId);
/*      */ 
/*  645 */       while (vsock.inputPkts > 0)
/*      */       {
/*  650 */         if (Logger.isActive()) {
/*  651 */           Logger.println("TdsSocket: Unread data in input packet queue");
/*      */         }
/*  653 */         dequeueInput(vsock);
/*      */       }
/*      */ 
/*  656 */       if (this.responseOwner != -1)
/*      */       {
/*  662 */         VirtualSocket other = (VirtualSocket)this.socketTable.get(this.responseOwner);
/*  663 */         byte[] tmpBuf = null;
/*  664 */         boolean ourData = other.owner == streamId;
/*      */         do
/*      */         {
/*  667 */           tmpBuf = readPacket((ourData) ? tmpBuf : null);
/*      */ 
/*  669 */           if (ourData) {
/*      */             continue;
/*      */           }
/*  672 */           enqueueInput(other, tmpBuf);
/*      */         }
/*      */ 
/*  675 */         while (tmpBuf[1] == 0);
/*      */       }
/*      */ 
/*  681 */       getOut().write(buffer, 0, getPktLen(buffer));
/*      */ 
/*  683 */       if (buffer[1] != 0) {
/*  684 */         getOut().flush();
/*      */ 
/*  686 */         this.responseOwner = streamId;
/*      */       }
/*      */ 
/*  689 */       return buffer;
/*      */     }
/*      */   }
/*      */ 
/*      */   byte[] getNetPacket(int streamId, byte[] buffer)
/*      */     throws IOException
/*      */   {
/*  703 */     synchronized (this.socketTable) {
/*  704 */       VirtualSocket vsock = lookup(streamId);
/*      */ 
/*  709 */       if (vsock.inputPkts > 0) {
/*  710 */         return dequeueInput(vsock);
/*      */       }
/*      */ 
/*  716 */       if (this.responseOwner == -1) {
/*  717 */         throw new IOException("Stream " + streamId + " attempting to read when no request has been sent");
/*      */       }
/*      */ 
/*  723 */       if (this.responseOwner != streamId)
/*      */       {
/*  725 */         throw new IOException("Stream " + streamId + " is trying to read data that belongs to stream " + this.responseOwner);
/*      */       }
/*      */ 
/*  732 */       return readPacket(buffer);
/*      */     }
/*      */   }
/*      */ 
/*      */   private void enqueueInput(VirtualSocket vsock, byte[] buffer)
/*      */     throws IOException
/*      */   {
/*  748 */     if ((globalMemUsage + buffer.length > memoryBudget) && (vsock.pktQueue.size() >= minMemPkts) && (!(securityViolation)) && (vsock.diskQueue == null))
/*      */     {
/*      */       try
/*      */       {
/*  754 */         vsock.queueFile = File.createTempFile("jtds", ".tmp", this.bufferDir);
/*      */ 
/*  756 */         vsock.diskQueue = new RandomAccessFile(vsock.queueFile, "rw");
/*      */ 
/*  761 */         while (vsock.pktQueue.size() > 0) {
/*  762 */           byte[] tmpBuf = (byte[])vsock.pktQueue.removeFirst();
/*  763 */           vsock.diskQueue.write(tmpBuf, 0, getPktLen(tmpBuf));
/*  764 */           vsock.pktsOnDisk += 1;
/*      */         }
/*      */       }
/*      */       catch (SecurityException se) {
/*  768 */         securityViolation = true;
/*  769 */         vsock.queueFile = null;
/*  770 */         vsock.diskQueue = null;
/*      */       }
/*      */     }
/*      */ 
/*  774 */     if (vsock.diskQueue != null)
/*      */     {
/*  776 */       vsock.diskQueue.write(buffer, 0, getPktLen(buffer));
/*  777 */       vsock.pktsOnDisk += 1;
/*      */     }
/*      */     else {
/*  780 */       vsock.pktQueue.addLast(buffer);
/*  781 */       globalMemUsage += buffer.length;
/*      */ 
/*  783 */       if (globalMemUsage > peakMemUsage) {
/*  784 */         peakMemUsage = globalMemUsage;
/*      */       }
/*      */     }
/*      */ 
/*  788 */     vsock.inputPkts += 1;
/*      */   }
/*      */ 
/*      */   private byte[] dequeueInput(VirtualSocket vsock)
/*      */     throws IOException
/*      */   {
/*  799 */     byte[] buffer = null;
/*      */ 
/*  801 */     if (vsock.pktsOnDisk > 0)
/*      */     {
/*  803 */       if (vsock.diskQueue.getFilePointer() == vsock.diskQueue.length())
/*      */       {
/*  805 */         vsock.diskQueue.seek(0L);
/*      */       }
/*      */ 
/*  808 */       vsock.diskQueue.readFully(this.hdrBuf, 0, 8);
/*      */ 
/*  810 */       int len = getPktLen(this.hdrBuf);
/*      */ 
/*  812 */       buffer = new byte[len];
/*  813 */       System.arraycopy(this.hdrBuf, 0, buffer, 0, 8);
/*  814 */       vsock.diskQueue.readFully(buffer, 8, len - 8);
/*  815 */       vsock.pktsOnDisk -= 1;
/*      */ 
/*  817 */       if (vsock.pktsOnDisk < 1)
/*      */         try
/*      */         {
/*  820 */           vsock.diskQueue.close();
/*  821 */           vsock.queueFile.delete();
/*      */         } finally {
/*  823 */           vsock.queueFile = null;
/*  824 */           vsock.diskQueue = null;
/*      */         }
/*      */     }
/*  827 */     else if (vsock.pktQueue.size() > 0) {
/*  828 */       buffer = (byte[])vsock.pktQueue.removeFirst();
/*  829 */       globalMemUsage -= buffer.length;
/*      */     }
/*      */ 
/*  832 */     if (buffer != null) {
/*  833 */       vsock.inputPkts -= 1;
/*      */     }
/*      */ 
/*  836 */     return buffer;
/*      */   }
/*      */ 
/*      */   private byte[] readPacket(byte[] buffer)
/*      */     throws IOException
/*      */   {
/*      */     try
/*      */     {
/*  851 */       getIn().readFully(this.hdrBuf);
/*      */     } catch (EOFException e) {
/*  853 */       throw new IOException("DB server closed connection.");
/*      */     }
/*      */ 
/*  856 */     byte packetType = this.hdrBuf[0];
/*      */ 
/*  858 */     if ((packetType != 2) && (packetType != 1) && (packetType != 15) && (packetType != 4))
/*      */     {
/*  862 */       throw new IOException("Unknown packet type 0x" + Integer.toHexString(packetType & 0xFF));
/*      */     }
/*      */ 
/*  867 */     int len = getPktLen(this.hdrBuf);
/*      */ 
/*  869 */     if ((len < 8) || (len > 65536)) {
/*  870 */       throw new IOException("Invalid network packet length " + len);
/*      */     }
/*      */ 
/*  873 */     if ((buffer == null) || (len > buffer.length))
/*      */     {
/*  875 */       buffer = new byte[len];
/*      */ 
/*  877 */       if (len > this.maxBufSize) {
/*  878 */         this.maxBufSize = len;
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  883 */     System.arraycopy(this.hdrBuf, 0, buffer, 0, 8);
/*      */     try
/*      */     {
/*  886 */       getIn().readFully(buffer, 8, len - 8);
/*      */     } catch (EOFException e) {
/*  888 */       throw new IOException("DB server closed connection.");
/*      */     }
/*      */ 
/*  897 */     if ((++this.packetCount == 1) && (this.serverType == 1) && ("NTLMSSP".equals(new String(buffer, 11, 7))))
/*      */     {
/*  899 */       buffer[1] = 1;
/*      */     }
/*      */ 
/*  902 */     synchronized (this.cancelMonitor)
/*      */     {
/*  909 */       if (this.cancelPending)
/*      */       {
/*  915 */         int frag = Math.min(9, len - 8);
/*  916 */         int keep = 9 - frag;
/*  917 */         System.arraycopy(this.doneBuffer, frag, this.doneBuffer, 0, keep);
/*  918 */         System.arraycopy(buffer, len - frag, this.doneBuffer, keep, frag);
/*  919 */         this.doneBufferFrag = Math.min(9, this.doneBufferFrag + frag);
/*      */ 
/*  922 */         if (this.doneBufferFrag < 9) {
/*  923 */           buffer[1] = 0;
/*      */         }
/*      */ 
/*  931 */         if (buffer[1] == 1) {
/*  932 */           if ((this.doneBuffer[0] & 0xFF) < 253) {
/*  933 */             throw new IOException("Expecting a TDS_DONE or TDS_DONEPROC.");
/*      */           }
/*      */ 
/*  936 */           if ((this.doneBuffer[1] & 0x20) != 0)
/*      */           {
/*  938 */             this.cancelPending = false;
/*      */           }
/*      */           else
/*      */           {
/*  942 */             buffer[1] = 0;
/*      */           }
/*      */         }
/*      */       }
/*      */ 
/*  947 */       if (buffer[1] != 0)
/*      */       {
/*  949 */         this.responseOwner = -1;
/*      */       }
/*      */     }
/*      */ 
/*  953 */     return buffer;
/*      */   }
/*      */ 
/*      */   private VirtualSocket lookup(int streamId)
/*      */   {
/*  962 */     if ((streamId < 0) || (streamId > this.socketTable.size())) {
/*  963 */       throw new IllegalArgumentException("Invalid parameter stream ID " + streamId);
/*      */     }
/*      */ 
/*  967 */     VirtualSocket vsock = (VirtualSocket)this.socketTable.get(streamId);
/*      */ 
/*  969 */     if (vsock.owner != streamId) {
/*  970 */       throw new IllegalStateException("Internal error: bad stream ID " + streamId);
/*      */     }
/*      */ 
/*  974 */     return vsock;
/*      */   }
/*      */ 
/*      */   static int getPktLen(byte[] buf)
/*      */   {
/*  985 */     int lo = buf[3] & 0xFF;
/*  986 */     int hi = (buf[2] & 0xFF) << 8;
/*      */ 
/*  988 */     return (hi | lo);
/*      */   }
/*      */ 
/*      */   protected void setTimeout(int timeout)
/*      */     throws SocketException
/*      */   {
/*  997 */     this.socket.setSoTimeout(timeout);
/*      */   }
/*      */ 
/*      */   protected void setKeepAlive(boolean keepAlive)
/*      */     throws SocketException
/*      */   {
/* 1006 */     this.socket.setKeepAlive(keepAlive);
/*      */   }
/*      */ 
/*      */   protected DataInputStream getIn()
/*      */   {
/* 1015 */     return this.in;
/*      */   }
/*      */ 
/*      */   protected void setIn(DataInputStream in)
/*      */   {
/* 1024 */     this.in = in;
/*      */   }
/*      */ 
/*      */   protected DataOutputStream getOut()
/*      */   {
/* 1033 */     return this.out;
/*      */   }
/*      */ 
/*      */   protected void setOut(DataOutputStream out)
/*      */   {
/* 1042 */     this.out = out;
/*      */   }
/*      */ 
/*      */   protected String getHost()
/*      */   {
/* 1051 */     return this.host;
/*      */   }
/*      */ 
/*      */   protected int getPort()
/*      */   {
/* 1060 */     return this.port;
/*      */   }
/*      */ 
/*      */   protected void finalize()
/*      */     throws Throwable
/*      */   {
/* 1067 */     close();
/* 1068 */     super.finalize();
/*      */   }
/*      */ 
/*      */   private static class VirtualSocket
/*      */   {
/*      */     final int owner;
/*      */     final LinkedList pktQueue;
/*      */     boolean flushInput;
/*      */     boolean complete;
/*      */     File queueFile;
/*      */     RandomAccessFile diskQueue;
/*      */     int pktsOnDisk;
/*      */     int inputPkts;
/*      */ 
/*      */     VirtualSocket(int streamId)
/*      */     {
/*  114 */       this.owner = streamId;
/*  115 */       this.pktQueue = new LinkedList();
/*  116 */       this.flushInput = false;
/*  117 */       this.complete = false;
/*  118 */       this.queueFile = null;
/*  119 */       this.diskQueue = null;
/*  120 */       this.pktsOnDisk = 0;
/*  121 */       this.inputPkts = 0;
/*      */     }
/*      */   }
/*      */ }

/* Location:           C:\Tomcat2\webapps\gateway2\WEB-INF\lib\jtds-1.2.6.jar
 * Qualified Name:     net.sourceforge.jtds.jdbc.SharedSocket
 * JD-Core Version:    0.5.3
 */