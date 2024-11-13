package core;

import utility.LogUtil;

public abstract class TaskAbstract implements Task {
	public void run() throws Exception {
		long end = 0;
		long start = System.currentTimeMillis();
		try {
			System.out.println(jobDescription() + " is starting ..................");
			execute();
			System.out.println(jobDescription() + " is ending ..................");
			end = System.currentTimeMillis() - start;
			LogUtil.logSuccess("Handle Success at " + jobDescription(), jobDescription(), end, "INF");
		} catch (Exception e) {
			System.out.println(e);
			throw e;
		}
	}
}
