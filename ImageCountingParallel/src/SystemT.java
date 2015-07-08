

public class SystemT
{
	private Task task[] = new Task[6];
	private ChildTask childTask[] = new ChildTask[36];
	private MainTask mainTask = new MainTask(this, task);

	public SystemT()
	{

		for(int i = 0; i < task.length; i++) {
			task[i] = new Task(i, mainTask, childTask);
			for(int j = 0; j < task.length; j++)
				childTask[j + (i*6)] = new ChildTask(j + (i*6), task[i], i);
		}
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
