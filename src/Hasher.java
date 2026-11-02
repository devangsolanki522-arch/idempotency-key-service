import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

  public class Hasher {
      public static String sha256(String input) throws NoSuchAlgorithmException {
          MessageDigest digest = MessageDigest.getInstance("SHA-256");
          byte[] inputBytes = input.getBytes(StandardCharsets.UTF_8);
          byte[] hash = digest.digest(inputBytes);
          StringBuilder hexString = new StringBuilder();
          for (byte b : hash) {
              hexString.append(String.format("%02x", b & 0xff));
          }
          return hexString.toString();
      }
}