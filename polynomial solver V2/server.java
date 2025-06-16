import java.io.*;
import java.net.*;
import java.util.*;

public class server {

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(5000);
        System.out.println("Server started on port 5000...");

        while (true) {
            Socket socket = serverSocket.accept();
            System.out.println("Client connected: " + socket);

            DataInputStream input = new DataInputStream(socket.getInputStream());
            DataOutputStream output = new DataOutputStream(socket.getOutputStream());

            int degree = input.readInt();
            double[] coeffs = new double[degree + 1];
            for (int i = 0; i <= degree; i++) {
                coeffs[i] = input.readDouble();
            }

            Complex[] roots = solvePolynomial(coeffs);
            output.writeInt(roots.length);
            for (Complex root : roots) {
                output.writeDouble(root.re);
                output.writeDouble(root.im);
            }

            socket.close();
        }
    }

    static Complex[] solvePolynomial(double[] coeffs) {
        int degree = coeffs.length - 1;
        if (degree == 1) return solveLinear(coeffs);
        if (degree == 2) return solveQuadratic(coeffs);
        return solveWithDurandKerner(coeffs);
    }

    static Complex[] solveLinear(double[] coeffs) {
        double a = coeffs[0], b = coeffs[1];
        return new Complex[]{new Complex(-b / a, 0)};
    }

    static Complex[] solveQuadratic(double[] coeffs) {
        double a = coeffs[0], b = coeffs[1], c = coeffs[2];
        double disc = b * b - 4 * a * c;
        if (disc >= 0) {
            double sqrtDisc = Math.sqrt(disc);
            return new Complex[]{
                new Complex((-b + sqrtDisc) / (2 * a), 0),
                new Complex((-b - sqrtDisc) / (2 * a), 0)
            };
        } else {
            double real = -b / (2 * a);
            double imag = Math.sqrt(-disc) / (2 * a);
            return new Complex[]{
                new Complex(real, imag),
                new Complex(real, -imag)
            };
        }
    }

    static Complex evaluate(double[] coeffs, Complex x) {
        Complex result = new Complex(0, 0);
        for (double coef : coeffs) {
            result = result.multiply(x).add(new Complex(coef, 0));
        }
        return result;
    }

    static Complex[] solveWithDurandKerner(double[] coeffs) {
        int n = coeffs.length - 1;
        Complex[] roots = new Complex[n];
        Complex[] newRoots = new Complex[n];
        double angle;
        for (int i = 0; i < n; i++) {
            angle = 2 * Math.PI * i / n;
            roots[i] = new Complex(Math.cos(angle), Math.sin(angle));
        }

        int maxIter = 100;
        double tol = 1e-8;

        for (int iter = 0; iter < maxIter; iter++) {
            boolean converged = true;
            for (int i = 0; i < n; i++) {
                Complex numerator = evaluate(coeffs, roots[i]);
                Complex denom = new Complex(1, 0);
                for (int j = 0; j < n; j++) {
                    if (i != j) denom = denom.multiply(roots[i].subtract(roots[j]));
                }
                newRoots[i] = roots[i].subtract(numerator.divide(denom));
                if (roots[i].subtract(newRoots[i]).abs() > tol) converged = false;
            }
            roots = Arrays.copyOf(newRoots, n);
            if (converged) break;
        }

        return roots;
    }

    static class Complex {
        double re, im;

        Complex(double r, double i) {
            re = r;
            im = i;
        }

        Complex add(Complex o) {
            return new Complex(re + o.re, im + o.im);
        }

        Complex subtract(Complex o) {
            return new Complex(re - o.re, im - o.im);
        }

        Complex multiply(Complex o) {
            return new Complex(re * o.re - im * o.im, re * o.im + im * o.re);
        }

        Complex divide(Complex o) {
            double denom = o.re * o.re + o.im * o.im;
            return new Complex((re * o.re + im * o.im) / denom,
                               (im * o.re - re * o.im) / denom);
        }

        double abs() {
            return Math.hypot(re, im);
        }
    }
}
