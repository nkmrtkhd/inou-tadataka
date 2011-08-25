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

  ArrayList<Double> pos= new ArrayList<Double>();
  double xmin,xmax,ymin,ymax;

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
      double tmp;
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
        tmp=epnum.getNumber();
        pos.add(tmp);
        if(tmp<xmin)xmin=tmp;
        if(tmp>xmax)xmax=tmp;
        //y
        epnum.setString( elem[1] );
        tmp=epnum.getNumber();
        pos.add(tmp);
        if(tmp<ymin)ymin=tmp;
        if(tmp>ymax)ymax=tmp;
      }

      myecho("---------------");
      myecho(String.format("loaded file: %s",filename));
      myecho(String.format("xmin= %.4e, xmax= %.4e",xmin,xmax));
      myecho(String.format("ymin= %.4e, ymax= %.4e",ymin,ymax));

    }catch (Exception e) {
    }
    myCanv.repaint();

  }
  private void myecho(String str){
    outArea.append(String.format("%s \n",str));
    outArea.setCaretPosition(outArea.getText().length());
  }
  private void convexHull(){
    ArrayList<Integer> q= new ArrayList<Integer>();
    q.add(0);
    for(int ii=0;ii<pos.size()/2;ii++){
      int i=q.get(ii);
      double xi=pos.get(2*i);
      double yi=pos.get(2*i+1);
      jLoop:for(int j=0;j<pos.size()/2;j++){
        if(i==j)continue;
        double xj=pos.get(2*j);
        double yj=pos.get(2*j+1);

        for(int kk=0;kk<q.size();kk++){
          int k=q.get(kk);
          if(k==i || k==j)continue;
          double xk=pos.get(2*k);
          double yk=pos.get(2*k+1);
          double drcz=(xj-xi)*(yk-yi)-(yj-yi)*(xk-xi);//drcz= n_z * (rij x rik)
          if(drcz<=0)continue jLoop;//もしも右回りなら,そのｊは無視
        }//kk
        q.add(j);
      }//j
    }//ii

    outer.clear();
    for(int ii=0;ii<q.size();ii++){
      int i=q.get(ii);
      outer.add(pos.get(2*i));
      outer.add(pos.get(2*i+1));
    }

    myCanv.repaint();
  }//convexhull

  ArrayList<Double> outer=new ArrayList<Double>();
  private void cal(){

    //convex hull
    //convexHull();


    //add simply
    outer.clear();
    for(int i=0;i<pos.size();i++)outer.add(pos.get(i));

    //paint outer points
    myCanv.repaint();


    //set grid
    int ng=((Integer)spGrid.getValue()).intValue();
    double dx=(xmax-xmin)/ng;
    double dy=(ymax-ymin)/ng;
    int[][] grid=new int[ng][ng];

    //init
    for(int i=0;i<ng;i++)for(int j=0;j<ng;j++)grid[i][j]=0;

    //loop for neighbor points
    for(int i=0;i<outer.size()/2-1;i++){
      System.out.println(String.format("loop: %d/%d",i,outer.size()/2));
      double ex1=outer.get(2*i);
      double ey1=outer.get(2*i+1);
      double ex2=outer.get(2*i+2);
      double ey2=outer.get(2*i+3);

      int gxs=(int)((ex1-xmin)/dx);
      int gxe=(int)((ex2-xmin)/dx);
      gxs=gxs%ng;
      gxe=gxe%ng;

      System.out.println(String.format("start, end: %d %d",gxs,gxe));
      if(gxs<0)System.out.println("negative gxs");
      if(gxe<0)System.out.println("negative gxe");

      if(gxs==gxe){
        /*
         * int gys=(int)((ey1-ymin)/dy);
         * int gye=(int)((ey2-ymin)/dy);
         * if(gys<0)System.out.println("negative gys");
         * if(gye<0)System.out.println("negative gye");
         * if(gys<gye)
         *   for(int igy=gys;igy<=gye;igy++)grid[gxs][igy]=1;
         * else
         *   for(int igy=gys;igy<=gye;igy--)grid[gxs][igy]=1;
         */

      }else if(gxs<gxe){
        for(int igx=gxs;igx<=gxe;igx++){
          double x=dx*igx+xmin;
          if(ex1<=x && x<=ex2){
          }else{
            /*
             * System.out.println("wow!!!");
             * System.out.println(String.format("x: %f %f",ex2,ex1));
             * System.out.println(String.format("y: %f %f",ey2,ey1));
             */
            continue;
          }
          double y=(ey2-ey1)/(ex2-ex1)*(x-ex1)+ey1-ymin;
          int igy=(int)(y/dy);
          if(igy<0)igy=0;
          if(igy>=ng)igy=ng-1;
          //System.out.println(String.format("%d %d ",igx,igy));
          grid[igx][igy]=1;
        }
      }else{
        for(int igx=gxe;igx<=gxs;igx++){
          double x=dx*igx+xmin;
          if(ex2<=x && x<=ex1){
          }else{
            //System.out.println("wow2");
            continue;
          }
          double y=(ey2-ey1)/(ex2-ex1)*(x-ex1)+ey1-ymin;
          int igy=(int)(y/dy);
          if(igy<0)igy=0;
          if(igy>=ng)igy=ng-1;
          grid[igx][igy]=1;
        }
      }
    }//i

    System.out.println("set grid done");
    //
    /// count innter area
    int inc=0;
    for(int ix=0;ix<ng;ix++){
      double x=xmin+dx*ix;
      //search y1,y2
      int ymax=-2*ng;
      int ymin=2*ng;
      for(int iy=0;iy<ng;iy++){
        if(grid[ix][iy]==1){
          if(iy<ymin)ymin=iy;
          if(iy>ymax)ymax=iy;
        }
      }
      if(ymax==-2*ng || ymin==2*ng)
        System.out.println(String.format("error at %d",ix));
      inc+=(ymax-ymin);
    }//ix


    myecho("---");
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
        int r=3;
        double dy=(ymax-ymin);
        double dx=(xmax-xmin);
        for(int i=0;i<pos.size()/2;i++){
          int x=(int)((pos.get(2*i)-xmin)*width/dx)+10;
          int y=h-(int)((pos.get(2*i+1)-ymin)*height/dy)-10;
          g.fill3DRect(x,y,r,r,false);
        }
      }

      g.setColor(Color.blue);
      int r=2;
      double dy=(ymax-ymin);
      double dx=(xmax-xmin);
      for(int i=0;i<outer.size()/2;i++){
        int x=(int)((outer.get(2*i)-xmin)*width/dx)+10;
        int y=h-(int)((outer.get(2*i+1)-ymin)*height/dy)-10;
        g.fill3DRect(x,y,r,r,false);
      }


    }
  }//end of mycanvas


}//end of this class
