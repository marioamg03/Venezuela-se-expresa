package bitsandpixels.instar;

/**
 * Created by Mario on 20/5/2017.
 */

public class Usuarios {
    private  String Imagen;
    private String Seudonimo;


    public Usuarios(){
    }

    public String getImagen() {
        return Imagen;
    }

    public void setImagen(String imagen) {
        Imagen = imagen;
    }

    public String getSeudonimo() {
        return Seudonimo;
    }

    public void setSeudonimo(String seudonimo) {
        Seudonimo = seudonimo;
    }

    public Usuarios(String imagen, String seudonimo){
        this.Imagen = imagen;
        this.Seudonimo = seudonimo;
    }



}
