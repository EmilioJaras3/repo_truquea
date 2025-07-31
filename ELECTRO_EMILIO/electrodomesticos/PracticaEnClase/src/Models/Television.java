package Models;

public class Television extends Electrodomestico{
    private int resolucion;
    private boolean sintonizadorTDT;
    private double precioPorResol;
    private double precioPorSintonizador;

    final int RESOLUCION = 20;
    final boolean SINTONIZADOR_TDT = false;

    public Television() {
        super();
        this.resolucion = RESOLUCION;
        this.sintonizadorTDT = SINTONIZADOR_TDT;
    }
    public Television(int precioBase, int peso){
        super();
        this.resolucion = RESOLUCION;
        this.sintonizadorTDT = SINTONIZADOR_TDT;
    }

    public Television(int resolucion, boolean sintonizadorTDT, int precioBase, String color,char consumoEnergetico, int peso){
        this.resolucion = resolucion;
        this.sintonizadorTDT = sintonizadorTDT;
    }

    public int getResolucion() {
        return resolucion;
    }

    public boolean isSintonizadorTDT() {
        return sintonizadorTDT;
    }

    public double precioFinal() {
        precioPorResol = super.precioFinal();
        precioPorSintonizador = super.precioFinal();
        if (resolucion > 40) {
            precioPorResol = precioPorResol * 1.3;
        } else if (sintonizadorTDT == true) {
            precioPorSintonizador += 50;
        }
        return precioPorSintonizador;
    }
}
