package a;

public class MyRun implements Runnable {

	@Override
	public void run() {

		while (true) {
			System.out.println("TEST");

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

}
