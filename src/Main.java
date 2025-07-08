import javax.swing.JFrame;

public class Main {

    static JFrame f;
    static String currentemail;
    public static JFrame getInstance() 
    {
        if (f == null) 
        {
            f = new JFrame();
            f.setSize(500, 400);
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        }
        return f;
    }
    public static void main(String[] args)
    {
        new LogIn();
    }
}
