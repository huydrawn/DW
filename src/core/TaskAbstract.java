package core;

public abstract class TaskAbstract implements Task {
	public void run() throws Exception {
		try {
			execute();
		} catch (Exception e) {
			
			throw e;
		}
	}
}
