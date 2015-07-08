

public class SystemT
{
	private Task task[] = new Task[6];
	private MainTask mainTask = new MainTask(this, task);

	public SystemT()
	{
		for(int i = 0; i < task.length; i++)
			task[i] = new Task(i, mainTask);
		mainTask.start();
		for(int i = 0; i < task.length; i++)
			task[i].start();
	}
	
	public String toString()
	{
		String str = "" + mainTask;
		for(int index = 0; index < task.length; index++)
			str = str + task[index];
		return str;
	}
	
	public static void main(String[] args)
	{
		new SystemT();
	}
}
