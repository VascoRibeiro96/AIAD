import repast.Experience1;
import repast.Experience2;
import repast.Experience3;
import repast.ExperienceTest;
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
            System.out.println("                  |     1 - Experiencia 1         |");
            System.out.println("                  |     2 - Experiencia 2         |");
            System.out.println("                  |     3 - Experiencia 3         |");
            System.out.println("                  |     4 - Experiencia 4         |");
            System.out.println("                  |     0 - Sair          |");
            System.out.println("                  =========================\n");
            String op ="";
            Scanner reader = new Scanner(System.in);
            System.out.println("Enter a number: ");
            opcao = reader.nextInt();
            reader.close();
            System.out.print("\n");
            SimInit init = new SimInit();
            switch (opcao) {
                case 1:
                    Experience1 exp = new Experience1();
                    exp.setNumDrivers(1);
                    exp.setNumParks(1);
                    init.loadModel(exp, "", false);
                    break;
                case 2:
                    Experience2 exp2 = new Experience2();
                    exp2.setNumDrivers(1);
                    exp2.setNumParks(2);
                    init.loadModel(exp2, "", false);
                    break;
                case 3:
                    Experience3 exp3 = new Experience3();
                    exp3.setNumDrivers(1);
                    exp3.setNumParks(2);
                    init.loadModel(exp3, "", false);
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