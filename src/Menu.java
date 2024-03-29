import agents.Experiences.*;
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
        System.out.println("\n\n### Parque de Estacionamento ###");
        System.out.println("\n                  =========================");
        System.out.println("                  |     1 - Experiencia 1         |");
        System.out.println("                  |     2 - Experiencia 2         |");
        System.out.println("                  |     3 - Experiencia 3         |");
        System.out.println("                  |     4 - Experiencia 4         |");
        System.out.println("                  |     5 - Experiencia 5         |");
        System.out.println("                  |     6 - Experiencia 6         |");
        System.out.println("                  |     7 - Experiencia 7         |");
        System.out.println("                  |     8 - Experiencia 8         |");
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
                Experience1 exp1 = new Experience1();
                exp1.setNumDrivers(1);
                exp1.setNumParks(1);
                init.loadModel(exp1, "", false);
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
                Experience4 exp4 = new Experience4();
                exp4.setNumDrivers(4);
                exp4.setNumParks(2);
                init.loadModel(exp4, "", false);
                break;
            case 5:
                Experience5 exp5 = new Experience5();
                exp5.setNumDrivers(1);
                exp5.setNumParks(1);
                init.loadModel(exp5, "", false);
                break;
            case 6:
                Experience6 exp6 = new Experience6();
                exp6.setNumDrivers(10);
                exp6.setNumParks(1);
                init.loadModel(exp6, "", false);
                break;
            case 7:
                Experience7 exp7 = new Experience7();
                exp7.setNumDrivers(15);
                exp7.setNumParks(6);
                init.loadModel(exp7, "", false);
                break;
            case 8:
                ExperienceTest exp = new ExperienceTest();
                exp.setNumDrivers(25);
                exp.setNumParks(10);
                init.loadModel(exp, "", false);
                break;
            default:
                System.out.println("Opção Inválida!");
                break;
        }
    }
}