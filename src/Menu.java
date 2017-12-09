import repast.ParkingModel;
import uchicago.src.sim.engine.SimInit;
import java.util.Scanner;


public class Menu {

    public static void main(String[] args) {
        menu();
        System.out.println("Programa encerrado.");
    }
    //  menu principal
    private static void menu() { // menu principal
        int opcao = 0;
        do {
            System.out.println("\n\n### Parque de Estacionamento ###");
            System.out.println("\n                  =========================");
            System.out.println("                  |     1 - Parking Model         |");
            System.out.println("                  |     2 - Opçao 2      |");
            System.out.println("                  |     3 - Opçao 3        |");
            System.out.println("                  |     4 - Opçao 4       |");
            System.out.println("                  |     0 - Sair          |");
            System.out.println("                  =========================\n");
            String op ="";
            Scanner reader = new Scanner(System.in);
            System.out.println("Enter a number: ");
            opcao = reader.nextInt();
            reader.close();
            System.out.print("\n");
            switch (opcao) {
                case 1:
                    SimInit init = new SimInit();
                    init.loadModel(new ParkingModel(), "", false);
                case 2:
                    break;
                case 3:
                    break;
                case 4:
                    break;
                case 0:
                    break;
                default:
                    System.out.println("Opção Inválida!");
                    break;
            }
        } while (opcao != 0);
    }
}