package core;

import utility.DatabaseUtil;

public class ProcessingTask extends TaskCronAbstract {

	public ProcessingTask(String cron) {
		super.setCron(cron);
	}

	@Override
	public void execute() throws Exception {
		var jbdi = DatabaseUtil.getJdbiConnectionToWareHouse();
		jbdi.useHandle(handle -> {
			handle.execute("CALL UpdateFactLiquidation");
		});
	}

	@Override
	public String jobDescription() {
		// TODO Auto-generated method stub
		return "Processing Data";
	}

}
