
public class TaskHolder {

	private static TaskHolder instance = null;
	private Task task[] = new Task[6];
	private ChildTask childTask[] = new ChildTask[36];
	private MainTask mainTask;
	
	protected TaskHolder(){
		this.mainTask = new MainTask();
		for(int i = 0; i < this.task.length; i++) {
			this.task[i] = new Task(i);
			for(int j = 0; j < task.length; j++)
				this.childTask[j + (i*6)] = new ChildTask(i, j + (i*6));
		}
	}

	public static TaskHolder getInstance(){
		if (instance == null){
			instance = new TaskHolder();
		}
		return instance;
	}

	public static MainTask getMainTask(){
		return TaskHolder.getInstance().mainTask;
	}
	
	public static Task getTaskByIndex(int i){
		return TaskHolder.getInstance().task[i];
	}
	
	public static ChildTask getChildTaskByIndex(int i){
		return TaskHolder.getInstance().childTask[i];
	}
	
	public static int getTaskSize(){
		return TaskHolder.getInstance().task.length;
	}
	
	public static int getChildTaskSize(){
		return TaskHolder.getInstance().childTask.length;
	}
}
