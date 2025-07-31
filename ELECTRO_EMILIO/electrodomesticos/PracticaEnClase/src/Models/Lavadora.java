package Models;

public class Lavadora extends Electrodomestico{
    private int carga;
    private double precioPerCarga;
    final int CARGA = 5;

    public Lavadora() {
        super();
        this.carga = CARGA;
    }

    public Lavadora(int precioBase, int peso) {
        super(precioBase, peso);
        this.carga = CARGA;
    }

    public Lavadora(int precioBase, String color, char consumoEnergia, int peso, int carga) {
        super(precioBase,color,consumoEnergia, peso);
        this.carga = carga;
    }

    public int getCarga() {
        return carga;
    }

    public double precioFinal() {
        precioPerCarga = super.precioFinal();
        if (carga>30){
            precioPerCarga+=50;
        }
        return precioPerCarga;
    }
}
