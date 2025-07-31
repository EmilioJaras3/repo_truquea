import Models.*;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner teclado = new Scanner(System.in);
        Electrodomestico[] electrodomesticos = new Electrodomestico[10];

           electrodomesticos[0] = new Electrodomestico();

             electrodomesticos[1] = new Electrodomestico(200, 30);

               electrodomesticos[2] = new Electrodomestico(150, "Negro", 'A', 25);

                electrodomesticos[3] = new Lavadora();
         electrodomesticos[4] = new Lavadora(300, 40);
          electrodomesticos[5] = new Lavadora(400, "Azul", 'B', 60, 35);

           electrodomesticos[6] = new Television();
           electrodomesticos[7] = new Television();
           electrodomesticos[8] = new Television();
           electrodomesticos[9] = new Television();

        boolean seguir = true;

        while (seguir) {
            System.out.println("1. Ver todo");
            System.out.println("2. Ver totales");
            System.out.println("3. Salir");
            System.out.print("elige entre las opciones  ");

            int opcion = teclado.nextInt();

            switch (opcion) {
                case 1:
                    verTodo(electrodomesticos);
                    break;
                case 2:
                    verTotales(electrodomesticos);
                    break;
                case 3:
                    seguir = false;
                    System.out.println("adiosss");
                    break;
                default:
                    System.out.println("No existe ");
            }
        }

        teclado.close();
    }

    public static void verTodo(Electrodomestico[] electrodomesticos) {
        double totalTodo = 0;
        double totalLavadoras = 0;
        double totalTelevisiones = 0;

           System.out.println("lista de productos ");

            int contador = 1;
             for (Electrodomestico electro : electrodomesticos) {



            if (electro != null) {
                double precio;


                if (electro instanceof Television) {
                    precio = electro.getPrecioBase();}
                else {
                    precio = electro.precioFinal();
                }

                totalTodo += precio;

                switch (electro.getClass().getSimpleName()) {
                    case "Lavadora":
                        totalLavadoras += precio;
                        System.out.println("Lavadora " + contador + ": " + precio + " pesos");
                        break;
                         case "Television":
                           totalTelevisiones += precio;
                        System.out.println("Television " + contador + ": " + precio + " pesos");
                        break;
                    default:
                        System.out.println("Electrodomestico " + contador + ": " + precio + " pesos");
                }
                contador++;
            }
        }

        System.out.println("totales");
        System.out.println("Todo junto: " + totalTodo + " pesos");
         System.out.println("Solo lavadoras: " + totalLavadoras + " pesos");
        System.out.println("Solo televisiones: " + totalTelevisiones + " pesos");
    }

    public static void verTotales(Electrodomestico[] electrodomesticos) {

        double totalTodo = 0;

        double totalLavadoras = 0;

          double totalTelevisiones = 0;


        for (Electrodomestico electro : electrodomesticos) {
            if (electro != null) {
                double precio;

                    if (electro instanceof Television) {
                    precio = electro.getPrecioBase();
                } else {
                    precio = electro.precioFinal();
                }

                totalTodo += precio;

                       switch (electro.getClass().getSimpleName()) {
                    case "Lavadora":
                        totalLavadoras += precio;
                        break;

                        case "Television":
                        totalTelevisiones += precio;
                        break;
                }
            }
        }

            System.out.println("solo totalees");
           System.out.println("Todo: " + totalTodo + " pesos");
           System.out.println("Lavadoras: " + totalLavadoras + " pesos");
          System.out.println("Televisiones: " + totalTelevisiones + " pesos");
    }
}