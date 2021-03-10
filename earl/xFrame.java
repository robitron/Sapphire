import java.awt.*;

public class xFrame extends Frame 
{
   public xFrame () {
     super();
   }

   public xFrame(String title) {
      super(title);
   }

   public boolean handleEvent(Event evt) {

     if (evt.id==Event.WINDOW_DESTROY) {
        Component c[]=getComponents();
        for (int x=0;x<c.length;x++)
           c[x].handleEvent(evt);
           dispose();
           System.exit(0);
           return false;
     }
     
     return super.handleEvent(evt);
  }
}
