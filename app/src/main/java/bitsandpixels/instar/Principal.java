package bitsandpixels.instar;

/**
 * Created by Mario on 13/5/2017.
 */

public class Principal {

    private  String Titulo;
    private String Contenido;
    private String Imagen;
    private String Video;
    private String ID_U;


    // NO BORRAR PRINCIPAL AUNQUE ESTE VACIO ESTO HACE ALGO (NO LO HE DESCUBIERTO TODAVIA) "MAGIA DE LA PROGRAMACION?"
        public Principal(){
        }

        public Principal(String Titulo, String Contenido, String Imagen, String Video, String ID_U) {
            this.Titulo = Titulo;
            this.Contenido = Contenido;
            this.Imagen = Imagen;
            this.Video = Video;
            this.ID_U = ID_U;
        }

        public String getTitulo() {
            return Titulo;
        }

        public void setTitulo(String titulo) {
            this.Titulo = titulo;
        }

        public String getContenido() {
            return Contenido;
        }

        public void setContenido(String contenido) {
            this.Contenido = contenido;
        }

        public String getImagen(){
            return Imagen;
        }

        public void setImagen(String imagen) {
            this.Imagen = imagen;
        }

        public String getVideo(){
        return Video;
    }

        public void setVideo(String video) {
        this.Video = video;
    }

        public String getID_U() {
            return ID_U;
        }

        public void setID_U(String ID_U) {
            this.ID_U = ID_U;
        }

}
