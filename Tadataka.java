import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.awt.image.*;
import javax.imageio.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;

//handmade library
import tools.*;


public class Tadataka implements ActionListener{

  //for GUI
  private JButton openButton;
  private JButton calButton;

  ArrayList<Float> pos= new ArrayList<Float>();
  float xmin,xmax,ymin,ymax;

  //main function
  public static void main(String[] args){
    if(args.length>0)
      new Tadataka(args[0]);
    else
      new Tadataka(null);
  }

  //constructor
  public Tadataka(String inputFile){
    makeControlFrame();
    if(inputFile!=null)this.open(inputFile);
  }


  public void actionPerformed(ActionEvent ae){

    String cmd = ae.getActionCommand();
    if(ae.getSource() == openButton){
      this.open(null);
    }else if(ae.getSource() == calButton){
      cal();
    }
    myCanv.repaint();

  }

  /** 引数がnullだったらダイアログで選んで，open */
  private void open(String filename){
    if(filename==null){
      String currentDir=System.getProperty("user.dir");
      JFileChooser jfc = new JFileChooser( (new File(currentDir)).getAbsolutePath() );

      jfc.setDialogTitle("select data");

      int s = jfc.showOpenDialog( null );
      if( s == JFileChooser.APPROVE_OPTION ){
        File file = jfc.getSelectedFile();
        filename = new String( file.getAbsolutePath() );
      }
    }
    pos.clear();
    //load image file
    try{
      FileReader fr = new FileReader( filename );
      BufferedReader br = new BufferedReader( fr );
      String line;
      String[] elem;
      Tokens tokens = new Tokens();
      Exponent epnum = new Exponent();
      float tmp;
      xmin=100;
      xmax=-100;
      ymin=100;
      ymax=-100;

      tokens.setDelim( " " );
      while((line = br.readLine())!=null){
        tokens.setString( line );
        elem = tokens.getTokens();

        //x
        epnum.setString( elem[0] );
        tmp=(float)(epnum.getNumber());
        pos.add(tmp);
        if(tmp<xmin)xmin=tmp;
        if(tmp>xmax)xmax=tmp;
        //y
        epnum.setString( elem[1] );
        tmp=(float)(epnum.getNumber());
        pos.add(tmp);
        if(tmp<ymin)ymin=tmp;
        if(tmp>ymax)ymax=tmp;
      }

      myecho(String.format("loaded file: %s",filename));

    }catch (Exception e) {
    }
    myCanv.repaint();

  }
  private void myecho(String str){
    outArea.append(String.format("%s \n",str));
    outArea.setCaretPosition(outArea.getText().length());
  }
  private void cal(){
    int ng=((Integer)spGrid.getValue()).intValue();

    double dx=(xmax-xmin)/ng;
    double dy=(ymax-ymin)/ng;


    int inc=0;
    //double area=0.;
    for(int ix=0;ix<=ng;ix++){
      double x=xmin+dx*ix;

      //search y1,y2
      double y1=1e10;
      double y2=-1e10;
      for(int i=0;i<pos.size()/2;i++){
        if(Math.abs(x-pos.get(2*i))<=dx){
          float tmp=pos.get(2*i+1);
          if(tmp<y1)y1=tmp;
          if(tmp>y2)y2=tmp;
        }
      }
      if(y1==1e10 || y2==-1e10){
        myecho("CANNOT CALCULATE because of too large grid!!");
        return;
      }

      //cal area 1
      for(int iy=0;iy<=ng;iy++){
        double y=ymin+dy*iy;
        if(y1<=y && y<=y2)inc++;
      }//iy
      //cal area 2
      //area+=(y2-y1)*dx;
    }//ix


    myecho("---------------");
    myecho(String.format("xmin= %f, xmax= %f",xmin,xmax));
    myecho(String.format("ymin= %f, ymax= %f",ymin,ymax));
    myecho(String.format("divided into %dx%d, unit are=%f",ng,ng,dx*dy));
    myecho(String.format("estimate area: %f",dx*dy*inc));
  }


  MyCanvas myCanv;
  JFrame ctrlJframe;
  JTextArea outArea;
  JSpinner spGrid;

  private Color borderColor=new Color(80,80,80);
  private Color innerBorderColor=new Color(80,80,80);
  private Color panelColor=new Color(200,200,200);
  private Color innerPanelColor=new Color(200,200,200);
  private void makeControlFrame(){
    ctrlJframe=new JFrame("伊能忠敬");
    //window size
    Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
    ctrlJframe.setBounds( 0, 0,600,800);
    //how to action, when close
    ctrlJframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


    //panel
    JPanel jp=new JPanel();

    openButton=new JButton("open");
    openButton.addActionListener( this );
    openButton.setFocusable(false);
    openButton.setActionCommand("open");

    calButton=new JButton("Cal.");
    calButton.addActionListener( this );
    calButton.setFocusable(false);

    JLabel lgrid = new JLabel( "Grid" );
    spGrid = new JSpinner(new SpinnerNumberModel(500, 1, null, 100));
    spGrid.setFocusable(false);
    spGrid.setPreferredSize(new Dimension(80, 28));
    // outout area
    outArea = new JTextArea();
    outArea.setEditable(false);
    outArea.setLineWrap(true);
    outArea.setCaretPosition(outArea.getText().length());
    JScrollPane sp = new JScrollPane(outArea,
                                     ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                                     ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
    sp.setPreferredSize(new Dimension(600, 300));

    myCanv=new MyCanvas();
    myCanv.setPreferredSize(new Dimension(400, 400));
    myCanv.setBackground(new Color(200,200,255));
    myCanv.repaint();

    SpringLayout layout = new SpringLayout();
    jp.setLayout(layout);

    layout.putConstraint( SpringLayout.NORTH,openButton, 5,SpringLayout.NORTH, jp);
    layout.putConstraint( SpringLayout.WEST,openButton, 5,SpringLayout.WEST, jp);
    layout.putConstraint( SpringLayout.NORTH,calButton, 0,SpringLayout.NORTH, openButton);
    layout.putConstraint( SpringLayout.WEST,calButton, 5,SpringLayout.EAST, openButton);

    layout.putConstraint( SpringLayout.NORTH,lgrid, 0,SpringLayout.NORTH, calButton);
    layout.putConstraint( SpringLayout.WEST,lgrid, 5,SpringLayout.EAST, calButton);
    layout.putConstraint( SpringLayout.NORTH,spGrid, 0,SpringLayout.NORTH, lgrid);
    layout.putConstraint( SpringLayout.WEST,spGrid, 5,SpringLayout.EAST, lgrid);


    layout.putConstraint( SpringLayout.NORTH,sp, 0,SpringLayout.SOUTH, openButton);
    layout.putConstraint( SpringLayout.WEST,sp, 5,SpringLayout.WEST, jp);
    layout.putConstraint( SpringLayout.NORTH,myCanv, 0,SpringLayout.SOUTH, sp);
    layout.putConstraint( SpringLayout.WEST,myCanv, 5,SpringLayout.WEST, jp);

    jp.add(openButton);
    jp.add(calButton);
    jp.add(lgrid);
    jp.add(spGrid);
    jp.add(sp);
    jp.add(myCanv);

    ctrlJframe.add(jp);
    ctrlJframe.setVisible(true);
  }


  /** private class for rendering image*/
  private class MyCanvas extends JPanel{
    public void paint(Graphics g){
      Graphics2D g2 = (Graphics2D)g;

      int w=getWidth();
      int h=getHeight();

      int width=w*9/10;
      int height=h*9/10;

      g2.clearRect(0, 0, w, h);
      //clear
      g.setColor(new Color(200,200,255));
      g.fill3DRect(0,0,w,h,false);

      g2.setStroke(new BasicStroke(1f)); //線の種類を設定

      if(pos.size()==0){
        g.drawLine(0,0,w/2,h/2);
      }else{
      g.setColor(Color.red);
      int r=2;
      for(int i=0;i<pos.size()/2;i++){
        int x=(int)((pos.get(2*i)-xmin)*width/(xmax-xmin))+10;
        int y=h-(int)((pos.get(2*i+1)-ymin)*height/(ymax-ymin))-10;
        g.fill3DRect(x,y,r,r,false);
      }
      }

    }
  }//end of mycanvas


}//end of this class
