package cc;

public class Main {

    public static void main(String[] args){
        GraphPanel gPanel = new GraphPanel(100);
        AppWindow win = new AppWindow();
        win.add(gPanel);
    }
}
