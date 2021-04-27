public class Stopwatch {

    private long elapsedTime;
    private long startTime;
    private boolean isRunning;

    public Stopwatch(){

        reset();
    }


    public void start(){

        if(isRunning) return;
        isRunning = true;
        startTime = System.currentTimeMillis();


    }


    public void stop() {

        if(!isRunning) return;

        isRunning = false;

        long endTime = System.currentTimeMillis();
        elapsedTime = elapsedTime + endTime - startTime;

    }

    public void reset(){
        elapsedTime = 0;
        isRunning = false;


    }

    public long getElapsedTime() {

        if(isRunning) {

            long endTime = System.currentTimeMillis();
            return elapsedTime + endTime - startTime;

        } else {

            return elapsedTime;


        }




    }
}