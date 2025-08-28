import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.math.BigInteger;

public class SecretSharing {

    // Decode a number with given base into BigInteger
    public static BigInteger decodeValue(String value, int base) {
        return new BigInteger(value, base);
    }

    // Lagrange interpolation at x = 0 (constant term = secret)
    public static BigInteger lagrangeInterpolation(List<Integer> xs, List<BigInteger> ys, int k) {
        BigInteger result = BigInteger.ZERO;

        for (int i = 0; i < k; i++) {
            // Start with yi
            BigInteger term = ys.get(i);

            // Multiply by product of (0 - xj)/(xi - xj)
            double fraction = 1.0;  // do fractions in double (since xs are small ints)
            for (int j = 0; j < k; j++) {
                if (i != j) {
                    fraction *= (0.0 - xs.get(j)) / (xs.get(i) - xs.get(j));
                }
            }

            // term * fraction, but fraction is double → multiply at the end
            double val = term.doubleValue() * fraction;
            result = result.add(BigInteger.valueOf(Math.round(val)));
        }

        return result;
    }

    public static void main(String[] args) throws Exception {
        // Read the whole JSON file
        String content = new String(Files.readAllBytes(Paths.get("testcase.json")));

        content = content.replaceAll("\\s+", ""); // remove spaces

        // Extract n and k
        int n = Integer.parseInt(content.replaceAll(".*\"n\":(\\d+).*", "$1"));
        int k = Integer.parseInt(content.replaceAll(".*\"k\":(\\d+).*", "$1"));

        List<Integer> xs = new ArrayList<>();
        List<BigInteger> ys = new ArrayList<>();

        // Very simple parsing: find entries like  "1":{"base":"10","value":"4"}
        String[] parts = content.split("\\},");
        for (String part : parts) {
            if (part.contains("\"keys\"")) continue;

            String keyStr = part.replaceAll(".*\"(\\d+)\":\\{.*", "$1");
            if (!keyStr.matches("\\d+")) continue;
            int x = Integer.parseInt(keyStr);

            String baseStr = part.replaceAll(".*\"base\":\"(\\d+)\".*", "$1");
            int base = Integer.parseInt(baseStr);

            String valStr = part.replaceAll(".*\"value\":\"([0-9a-zA-Z]+)\".*", "$1");

            BigInteger y = decodeValue(valStr, base);

            xs.add(x);
            ys.add(y);
        }

        // Use only first k points
        List<Integer> xs_k = xs.subList(0, k);
        List<BigInteger> ys_k = ys.subList(0, k);

        BigInteger secret = lagrangeInterpolation(xs_k, ys_k, k);

        System.out.println("Secret (c) = " + secret);
    }
}

