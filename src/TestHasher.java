public class TestHasher {

    public static void main(String[] args) throws Exception {
        String hash = Hasher.sha256("hello");

        System.out.println(hash);
        System.out.println(hash.length());
    }
}