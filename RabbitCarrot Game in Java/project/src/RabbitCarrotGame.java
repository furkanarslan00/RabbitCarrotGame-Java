import java.util.Random;
import java.util.Scanner;

public class RabbitCarrotGame {
    private final int numRabbits;
    private final int numBoxes;
    private final int carrotRate;
    private final int carrotTimeout;
    private final int rabbitSleepTime;
    private final Rabbit[] rabbits;
    private final Person person;
    private final Object[] boxLocks;
    private final boolean[] boxHasCarrot;
    private final boolean[] rabbitAteCarrot;
    private final Random random = new Random();

public RabbitCarrotGame(int numRabbits, int numBoxes, int carrotRate, int carrotTimeout, int rabbitSleepTime){
    this.numRabbits = numRabbits;
    this.numBoxes = numBoxes;
    this.carrotRate = carrotRate;
    this.carrotTimeout = carrotTimeout;
    this.rabbitSleepTime = rabbitSleepTime;
    this.rabbits = new Rabbit[numRabbits];
    this.boxLocks = new Object[numBoxes];
    this.boxHasCarrot = new boolean[numBoxes];
    this.rabbitAteCarrot = new boolean[numBoxes];

    for (int i = 0; i < numRabbits; i++)
        rabbits[i] = new Rabbit("Rabbit" + (i + 1));

    for (int i = 0; i < numBoxes; i++)
        boxLocks[i] = new Object();

    this.person = new Person();
}

public void startGame() {
    System.out.println("Game Starts...");
    Thread personThread = new Thread(person);
    personThread.start();
    Thread[] rabbitThreads = new Thread[numRabbits];
        
    for(int i = 0; i < numRabbits; i++) {
       rabbitThreads[i] = new Thread(rabbits[i]);
       rabbitThreads[i].start();}

    try {
         for(Thread rabbitThread : rabbitThreads)
             rabbitThread.join();

    person.stop();
    personThread.join();
    }catch (InterruptedException e) {
    e.printStackTrace();}

    System.out.println("Game Over!");

    for (Rabbit rabbit : rabbits)
        System.out.println(rabbit.getName() + " has " + rabbit.getScore() + " points");
}

public class Rabbit implements Runnable{
    private final String name;
    private int position;
    private int score;
    private Rabbit(String name)
    {this.name = name;}

@Override
public void run(){
    while(position < numBoxes - 1 && score < 5) {
         synchronized (boxLocks[position]) {
         int nextPosition = position + 1;
         System.out.println(name + " jumps to box " + nextPosition);
         position = nextPosition;

         if(!rabbitAteCarrot[position] && person.carrotInBox(position)){
            eatCarrot();}
            printRabbitState();
         }

         try{
             Thread.sleep(rabbitSleepTime);
             }catch (InterruptedException e){
             Thread.currentThread().interrupt();
             }
     }

        System.out.println(name + " has " + score + " points");
}

        public void printRabbitState(){
            System.out.println(name + " has " + score + " points");}

        public void eatCarrot(){
            rabbitAteCarrot[position] = true;
            score++;
            System.out.println(name + " Eats carrot in box " + position);}

        public String getName(){
            return name;}

        public int getScore() {
            return score;}
}

public class Person implements Runnable {
        private volatile boolean running = true;
        
        public void stop(){
            running = false;
        }
        
   @Override
   public void run(){
        try{
            while (running) {
            int randomBoxIndex = random.nextInt(numBoxes);
            synchronized (boxLocks[randomBoxIndex]){
            if(!boxHasCarrot[randomBoxIndex]) {
               personPutsCarrot(randomBoxIndex);
               boxLocks[randomBoxIndex].notifyAll();
               boxLocks[randomBoxIndex].wait(carrotTimeout);}}

               synchronized (boxLocks[randomBoxIndex]) {
               if(boxHasCarrot[randomBoxIndex]) {
               carrotRemoved(randomBoxIndex);}
               
               else {
                    carrotRemovedTimeout(randomBoxIndex);}
               
               }

               try {
                    Thread.sleep(carrotRate);
                    }catch (InterruptedException e) {
                    Thread.currentThread().interrupt();}
             }
           }catch (Exception e) {
                e.printStackTrace();}           
    }

        public boolean carrotInBox(int box) {
          return boxHasCarrot[box];}

        public void personPutsCarrot(int box) {
          boxHasCarrot[box] = true;
          rabbitAteCarrot[box] = false;
          System.out.println("Person Puts Carrot to Box " + box);}

        public void carrotRemoved(int box) {
          System.out.println("Carrot in box " + box + " Removed");
          boxHasCarrot[box] = false;}

        public void carrotRemovedTimeout(int box) {
          System.out.println("Carrot in box " + box + " Removed (timeout)");} 
        
}  
    public static void main(String[] args){
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter number of rabbits: ");
        int numRabbits = scanner.nextInt();

        System.out.print("Enter number of boxes: ");
        int numBoxes = scanner.nextInt();

        System.out.print("Enter carrot producing rate (X): ");
        int carrotRate = scanner.nextInt();

        System.out.print("Enter carrot timeout (Y): ");
        int carrotTimeout = scanner.nextInt();

        System.out.print("Enter sleeping time for rabbits (Z): ");
        int rabbitSleepTime = scanner.nextInt();

        RabbitCarrotGame game = new RabbitCarrotGame(numRabbits, numBoxes, carrotRate, carrotTimeout, rabbitSleepTime);
        game.startGame();
        
        scanner.close();
    }
    
}
