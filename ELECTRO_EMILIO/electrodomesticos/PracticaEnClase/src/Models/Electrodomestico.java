package Models;

public class Electrodomestico {
    protected int precioBase;
    protected String color;
    protected char consumoEnergetico;
    protected int peso;

    //constantes
    final String COLOR = "Blanco";
    final char CONSUMO_ENERGETICO = 'f';
    final int PRECIO_BASE = 100;
    final int PESO = 50;
    final String[] colores = {"Blanco", "Negro", "Azul", "Rojo", "Gris"};

    //constructores
    public Electrodomestico() {
        this.precioBase = PRECIO_BASE;
        this.color = COLOR;
        this.consumoEnergetico = comprobarCosumoEnergetico(consumoEnergetico);
        this.peso = PESO;
    }
    public Electrodomestico(int precioBase, int peso) {
        this.precioBase = precioBase;
        this.peso = peso;
        this.color = COLOR;
        this.consumoEnergetico = CONSUMO_ENERGETICO;
    }

    public Electrodomestico(int precioBase, String color, char consumoEnergetico, int peso) {
        this.precioBase = precioBase;
        this.color = color;
        this.consumoEnergetico = consumoEnergetico;
        this.peso = peso;
    }

    //metodos
    public int getPrecioBase() {
        return precioBase;
    }

    public String getColor() {
        return color;
    }

    public char getConsumoEnergetico() {
        return consumoEnergetico;
    }

    public int getPeso() {
        return peso;
    }

    public char comprobarCosumoEnergetico(char letra){
       char[] letras = {'A', 'B', 'C', 'D', 'E', 'F'};
       boolean flag = false;
       for (int i = 0; i < letras.length && !flag; i++) {
           if (letras[i] == letra) {
               flag = true;
           }
       }
       return (flag) ? letra : CONSUMO_ENERGETICO;
    }

    private String comprobarColor(String color){
        String[] colors = {"Blanco", "Negro", "Azul", "Rojo", "Gris"};
        boolean flag = false;
        for (int i = 0; i < colors.length && !flag; i++) {
            if (colors[i].equals(color)) {
                flag = true;
            }
        }return (flag) ? color : COLOR;
    }

    public double precioFinal() {
        double precioFinal=0;
        switch (consumoEnergetico){
            case 'A' -> precioFinal = 100;
            case 'B' -> precioFinal = 80;
            case 'C' -> precioFinal = 60;
            case 'D' -> precioFinal = 50;
            case 'E' -> precioFinal = 30;
            case 'F' -> precioFinal = 10;
            default -> System.out.println("El consumo energetico no es valido");
        }

        if (peso>0 && peso<19){
            precioFinal+=10;
        } else if (peso>20 && peso<49){
            precioFinal+=50;
        } else if(peso>50 && peso<79){
            precioFinal+=80;
        } else if(peso>80){
            precioFinal+=100;
        }
        return precioFinal;
    }
}
