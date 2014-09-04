
/**
 * GATEWAY_WEBSERVICES_LOTECallbackHandler.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.5.1  Built on : Oct 19, 2009 (10:59:00 EDT)
 */


    /**
     *  GATEWAY_WEBSERVICES_LOTECallbackHandler Callback class, Users can extend this class and implement
     *  their own receiveResult and receiveError methods.
     */
    public abstract class GATEWAY_WEBSERVICES_LOTECallbackHandler{



    protected Object clientData;

    /**
    * User can pass in any object that needs to be accessed once the NonBlocking
    * Web service call is finished and appropriate method of this CallBack is called.
    * @param clientData Object mechanism by which the user can pass in user data
    * that will be avilable at the time this callback is called.
    */
    public GATEWAY_WEBSERVICES_LOTECallbackHandler(Object clientData){
        this.clientData = clientData;
    }

    /**
    * Please use this constructor if you don't want to set any clientData
    */
    public GATEWAY_WEBSERVICES_LOTECallbackHandler(){
        this.clientData = null;
    }

    /**
     * Get the client data
     */

     public Object getClientData() {
        return clientData;
     }

        
           /**
            * auto generated Axis2 call back method for gatewayManualRetakeLote method
            * override this method for handling normal response from gatewayManualRetakeLote operation
            */
           public void receiveResultgatewayManualRetakeLote(
                    br.com.neurotech.gateway2.lote.webservice.GATEWAY_WEBSERVICES_LOTEStub.GatewayManualRetakeLoteResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from gatewayManualRetakeLote operation
           */
            public void receiveErrorgatewayManualRetakeLote(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for gatewayPauseLote method
            * override this method for handling normal response from gatewayPauseLote operation
            */
           public void receiveResultgatewayPauseLote(
                    br.com.neurotech.gateway2.lote.webservice.GATEWAY_WEBSERVICES_LOTEStub.GatewayPauseLoteResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from gatewayPauseLote operation
           */
            public void receiveErrorgatewayPauseLote(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for gatewayGetHeaderLote method
            * override this method for handling normal response from gatewayGetHeaderLote operation
            */
           public void receiveResultgatewayGetHeaderLote(
                    br.com.neurotech.gateway2.lote.webservice.GATEWAY_WEBSERVICES_LOTEStub.GatewayGetHeaderLoteResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from gatewayGetHeaderLote operation
           */
            public void receiveErrorgatewayGetHeaderLote(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for gatewayInsertLote method
            * override this method for handling normal response from gatewayInsertLote operation
            */
           public void receiveResultgatewayInsertLote(
                    br.com.neurotech.gateway2.lote.webservice.GATEWAY_WEBSERVICES_LOTEStub.GatewayInsertLoteResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from gatewayInsertLote operation
           */
            public void receiveErrorgatewayInsertLote(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for gatewayEstatisticaDetalheLote method
            * override this method for handling normal response from gatewayEstatisticaDetalheLote operation
            */
           public void receiveResultgatewayEstatisticaDetalheLote(
                    br.com.neurotech.gateway2.lote.webservice.GATEWAY_WEBSERVICES_LOTEStub.GatewayEstatisticaDetalheLoteResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from gatewayEstatisticaDetalheLote operation
           */
            public void receiveErrorgatewayEstatisticaDetalheLote(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for gatewayCancelLote method
            * override this method for handling normal response from gatewayCancelLote operation
            */
           public void receiveResultgatewayCancelLote(
                    br.com.neurotech.gateway2.lote.webservice.GATEWAY_WEBSERVICES_LOTEStub.GatewayCancelLoteResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from gatewayCancelLote operation
           */
            public void receiveErrorgatewayCancelLote(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getVersion method
            * override this method for handling normal response from getVersion operation
            */
           public void receiveResultgetVersion(
                    br.com.neurotech.gateway2.lote.webservice.GATEWAY_WEBSERVICES_LOTEStub.GetVersionResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getVersion operation
           */
            public void receiveErrorgetVersion(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for gatewayRestartLote method
            * override this method for handling normal response from gatewayRestartLote operation
            */
           public void receiveResultgatewayRestartLote(
                    br.com.neurotech.gateway2.lote.webservice.GATEWAY_WEBSERVICES_LOTEStub.GatewayRestartLoteResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from gatewayRestartLote operation
           */
            public void receiveErrorgatewayRestartLote(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for gatewayGetLote method
            * override this method for handling normal response from gatewayGetLote operation
            */
           public void receiveResultgatewayGetLote(
                    br.com.neurotech.gateway2.lote.webservice.GATEWAY_WEBSERVICES_LOTEStub.GatewayGetLoteResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from gatewayGetLote operation
           */
            public void receiveErrorgatewayGetLote(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for gatewayResultadoLote method
            * override this method for handling normal response from gatewayResultadoLote operation
            */
           public void receiveResultgatewayResultadoLote(
                    br.com.neurotech.gateway2.lote.webservice.GATEWAY_WEBSERVICES_LOTEStub.GatewayResultadoLoteResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from gatewayResultadoLote operation
           */
            public void receiveErrorgatewayResultadoLote(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for gatewayLogLote method
            * override this method for handling normal response from gatewayLogLote operation
            */
           public void receiveResultgatewayLogLote(
                    br.com.neurotech.gateway2.lote.webservice.GATEWAY_WEBSERVICES_LOTEStub.GatewayLogLoteResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from gatewayLogLote operation
           */
            public void receiveErrorgatewayLogLote(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for gatewayEstatisticaLote method
            * override this method for handling normal response from gatewayEstatisticaLote operation
            */
           public void receiveResultgatewayEstatisticaLote(
                    br.com.neurotech.gateway2.lote.webservice.GATEWAY_WEBSERVICES_LOTEStub.GatewayEstatisticaLoteResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from gatewayEstatisticaLote operation
           */
            public void receiveErrorgatewayEstatisticaLote(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for gatewayHistoricoDetalheLote method
            * override this method for handling normal response from gatewayHistoricoDetalheLote operation
            */
           public void receiveResultgatewayHistoricoDetalheLote(
                    br.com.neurotech.gateway2.lote.webservice.GATEWAY_WEBSERVICES_LOTEStub.GatewayHistoricoDetalheLoteResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from gatewayHistoricoDetalheLote operation
           */
            public void receiveErrorgatewayHistoricoDetalheLote(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for gatewayHistoricoLote method
            * override this method for handling normal response from gatewayHistoricoLote operation
            */
           public void receiveResultgatewayHistoricoLote(
                    br.com.neurotech.gateway2.lote.webservice.GATEWAY_WEBSERVICES_LOTEStub.GatewayHistoricoLoteResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from gatewayHistoricoLote operation
           */
            public void receiveErrorgatewayHistoricoLote(java.lang.Exception e) {
            }
                


    }
    