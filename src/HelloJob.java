import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Data de Criação: 19/08/2013
 * 
 * @author jjcc
 * @since XXX_vYYYYMMa
 * @version XXX_vYYYYMMa
 */
public class HelloJob implements Job {

	/**
	 * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
	 */
	@Override
	public void execute(JobExecutionContext pArg0) throws JobExecutionException {

		System.out.println("hello Job 1");
	}

}
