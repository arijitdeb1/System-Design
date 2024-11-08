public class Main {
    public static void main(String[] args) {
        //find prime numbers between 1 and 20
        for (int i = 1; i <= 20; i++) {
            boolean isPrime = true;
            for (int j = 2; j < i; j++) {
                if (i % j == 0) {
                    isPrime = false;
                    break;
                }
            }
            if (isPrime) {
                System.out.println(i + " is a prime number");
            }
        }
    }
}