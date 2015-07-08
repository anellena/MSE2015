

public class SystemT
{
	public SystemT()
	{

		TaskHolder.getInstance();
		TaskHolder.getMainTask().start();
		
		try {
			TaskHolder.getMainTask().join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/*public String toString()
	{
		String str = "" + mainTask;
		for(int index = 0; index < task.length; index++)
			str = str + task[index];
		return str;
	}*/
	
	public static void main(String[] args)
	{
		new SystemT();
	}
}
