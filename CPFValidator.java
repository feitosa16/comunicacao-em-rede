import java.util.Scanner;

public class CPFValidator {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Digite o CPF (apenas números): ");
        String cpf = scanner.nextLine();

        if (isValidCPF(cpf)) {
            System.out.println("CPF válido.");
        } else {
            System.out.println("CPF inválido.");
        }

        scanner.close();
    }

    public static boolean isValidCPF(String cpf) {
        cpf = cpf.replaceAll("[^\\d]", ""); // Remove caracteres não numéricos
        if (cpf.length() != 11 || cpf.matches("(\\d)\\1{10}")) return false; // Tamanho e sequência

        int[] weights1 = {10, 9, 8, 7, 6, 5, 4, 3, 2};
        int[] weights2 = {11, 10, 9, 8, 7, 6, 5, 4, 3, 2};
        int digit1 = calculateDigit(cpf.substring(0, 9), weights1);
        int digit2 = calculateDigit(cpf.substring(0, 10), weights2);
        return cpf.equals(cpf.substring(0, 9) + digit1 + digit2);
    }

    private static int calculateDigit(String str, int[] weights) {
        int sum = 0;
        for (int i = 0; i < str.length(); i++) sum += (str.charAt(i) - '0') * weights[i];
        int result = 11 - (sum % 11);
        return result > 9 ? 0 : result;
    }
}
