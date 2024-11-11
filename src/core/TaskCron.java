package core;

public interface TaskCron {
	public void run();

	public void execute() throws Exception;

	public String jobDescription();
}
