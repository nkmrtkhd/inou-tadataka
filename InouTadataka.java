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


public class InouTadataka implements ActionListener{

  //for GUI
  private JButton openButton;
  private JButton calButton;
  private JCheckBox cbConvexHull;
  private JCheckBox cbSort;
  private JCheckBox cbInner;

  ArrayList<Double> posIn= new ArrayList<Double>();
  ArrayList<Double> pos= new ArrayList<Double>();
  ArrayList<Integer> inner= new ArrayList<Integer>();
  double xmin,xmax,ymin,ymax;
  int xminID=0;

  //main function
  public static void main(String[] args){

    if(args.length>0)
      new InouTadataka(args[0]);
    else
      new InouTadataka(null);
  }

  //constructor
  public InouTadataka(String inputFile){
    setLookAndFeel();
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
    posIn.clear();
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
      int inc=0;
      while((line = br.readLine())!=null){
        tokens.setString( line );
        elem = tokens.getTokens();

        //x
        epnum.setString( elem[0] );
        tmp=epnum.getNumber();
        posIn.add(tmp);
        inc++;
        if(tmp<xmin){
          xmin=tmp;
          xminID=inc;
        }
        if(tmp>xmax)xmax=tmp;
        //y
        epnum.setString( elem[1] );
        tmp=epnum.getNumber();
        posIn.add(tmp);
        if(tmp<ymin)ymin=tmp;
        if(tmp>ymax)ymax=tmp;
      }

      myecho("");
      myecho("-- FILE --");
      myecho(String.format("Loaded file: %s",filename));
      myecho(String.format("(xmin, xmax)= (%.5e, %.5e)",xmin,xmax));
      myecho(String.format("(ymin, ymax)= (%.5e, %.5e)",ymin,ymax));
      outer.clear();
    }catch (Exception e) {
    }
    myCanv.repaint();
  }

  private void myecho(String str){
    outArea.append(String.format("%s \n",str));
    outArea.setCaretPosition(outArea.getText().length());
  }

  private ArrayList<Double> counterClockWiseSort(  ArrayList<Double> tmpPos ){
    double xg=0;
    double yg=0;
    for(int i=0;i<tmpPos.size()/2;i++){
      xg+=tmpPos.get(2*i);
      yg+=tmpPos.get(2*i+1);
    }
    xg/=tmpPos.size();
    yg/=tmpPos.size();

    LinkedList<Polar> queue= new LinkedList<Polar>();
    iLoop:for(int i=0;i<tmpPos.size()/2;i++){
      double theta=Math.atan2(tmpPos.get(2*i)-xg,tmpPos.get(2*i+1)-yg);

      for(int j=0;j<queue.size();j++){
        if(queue.get(j).theta<theta){
          queue.add(j,new Polar(i,theta));
          continue iLoop;
        }
      }//j
      queue.addLast(new Polar(i,theta));
    }//i

    ArrayList<Double> sorted= new ArrayList<Double>();

    double tmp=0;
    for(int ii=0;ii<queue.size();ii++){
      int i=queue.get(ii).id;
      if(tmpPos.get(2*i)<tmp){
        tmp=tmpPos.get(2*i);
        xminID=ii;
      }
      sorted.add( tmpPos.get(2*i)   );
      sorted.add( tmpPos.get(2*i+1) );
    }
    return sorted;
  }
  private class Polar{
    public int id;
    public double theta;
    public Polar(int id, double theta){
      this.id=id;
      this.theta=theta;
    }
  }
  private void convexHull(){
    ArrayList<Integer> q= new ArrayList<Integer>();
    q.add(xminID);
    for(int ii=0;ii<pos.size()/2;ii++){
      if(ii>=q.size())break;
      int i=q.get(ii);
      double xi=pos.get(2*i);
      double yi=pos.get(2*i+1);
      jLoop:for(int j=0;j<pos.size()/2;j++){
        if(i==j)continue;
        double xj=pos.get(2*j);
        double yj=pos.get(2*j+1);

        for(int k=0;k<pos.size()/2;k++){
          if(k==i || k==j)continue;
          double xk=pos.get(2*k);
          double yk=pos.get(2*k+1);
          double drcz=(xj-xi)*(yk-yi)-(yj-yi)*(xk-xi);//drcz= n_z * (rij x rik)_z
          if(drcz<0)continue jLoop;//もしもjikが右回りなら,そのjは無視
        }//kk

        q.add(j);
        break;
      }//j
    }//ii

    //check
    if(q.size()<3)myecho("Error at convex-hull");

    //make outer
    outer.clear();
    for(int ii=1;ii<q.size();ii++){
      int i=q.get(ii);
      outer.add(pos.get(2*i));
      outer.add(pos.get(2*i+1));
    }

    myCanv.repaint();
  }//convexhull

  ArrayList<Double> outer=new ArrayList<Double>();
  private void cal(){

    pos.clear();
    if(cbSort.isSelected()){
      pos=counterClockWiseSort(posIn);
    }else{
      double tmp=0;
      for(int i=0;i<posIn.size()/2;i++){
        if(posIn.get(2*i)<tmp){
          tmp=posIn.get(2*i);
          xminID=i;
        }
        pos.add(posIn.get(2*i));
        pos.add(posIn.get(2*i+1));
      }
    }

    if(cbConvexHull.isSelected()){
      convexHull();
    }else{
      //add simply
      outer.clear();
      for(int i=0;i<pos.size();i++)outer.add(pos.get(i));
    }

    //paint outer points
    myCanv.repaint();


    //set grid
    int ng=((Integer)spGrid.getValue()).intValue();
    double dx=(xmax-xmin)/(double)ng;
    double dy=(ymax-ymin)/(double)ng;
    int[][] grid=new int[ng+1][ng+1];

    //init
    for(int i=0;i<ng;i++)for(int j=0;j<ng;j++)grid[i][j]=0;

    //loop for neighbor points
    for(int i=0;i<outer.size()/2-1;i++){
      double ex1=outer.get(2*i);
      double ey1=outer.get(2*i+1);
      double ex2=outer.get(2*i+2);
      double ey2=outer.get(2*i+3);

      int nx1=(int)((ex1-xmin)/dx);
      int nx2=(int)((ex2-xmin)/dx);
      if(nx1<0){
        System.out.println(String.format("nx1 %d<0 at %d",nx1,i));
        nx1=0;
      }
      if(nx1>ng){
        System.out.println(String.format("nx1 %d>ng at %d",nx1,i));
        nx1=ng-1;
      }
      if(nx2<0){
        System.out.println(String.format("nx1 %d<0 at %d",nx2,i));
        nx2=0;
      }
      if(nx2>ng){
        System.out.println(String.format("nx1 %d>ng at %d",nx2,i));
        nx2=ng-1;
      }

      //set grid[][]
      if(nx1==nx2){
        int ny1=(int)((ey1-ymin)/dy);
        int ny2=(int)((ey2-ymin)/dy);
        if(ny1<0){
          System.out.println(String.format("ny1 %d<0 at %d",ny1,i));
          ny1=0;
        }
        if(ny1>ng){
          System.out.println(String.format("ny1 %d>ng at %d",ny1,i));
          ny1=ng-1;
        }
        if(ny2<0){
          System.out.println(String.format("ny1 %d<0 at %d",ny2,i));
          ny2=0;
        }
        if(ny2>ng){
          System.out.println(String.format("ny1 %d>ng at %d",ny2,i));
          ny2=ng-1;
        }

        if(ny1<ny2)
          for(int igy=ny1;igy<=ny2;igy++)grid[nx1][igy]=1;
        else
          for(int igy=ny2;igy<=ny1;igy++)grid[nx1][igy]=1;

      }else if(ex1<ex2){
        for(int igx=nx1;igx<=nx2;igx++){
          double x=dx*igx+xmin;
          if(ex1<=x && x<=ex2){
            double y=(ey2-ey1)/(ex2-ex1)*(x-ex1)+ey1-ymin;
            int igy=(int)(y/dy);
            if(igy<0){
              System.out.println(String.format("igy %d<0 at %d",igy,i));
              igy=0;
            }
            if(igy>ng){
              System.out.println(String.format("igy %d>ng at %d",igy,i));
              igy=ng-1;
            }
            grid[igx][igy]=1;
          }else{
            /*
             * System.out.println(String.format("out of range at %d",igx));
             * System.out.println(String.format("  %d to %d",nx1,nx2));
             * System.out.println(String.format("   %e < %e < %e",ex1,x,ex2));
             * System.out.println(String.format("   %e, %d, %e",dx,igx,xmin));
             */
          }
        }
      }else{
        for(int igx=nx2;igx<=nx1;igx++){
          double x=dx*igx+xmin;
          if(ex2<=x && x<=ex1){
            double y=(ey2-ey1)/(ex2-ex1)*(x-ex1)+ey1-ymin;
            int igy=(int)(y/dy);
            if(igy<0){
              System.out.println(String.format("igy %d<0 at %d",igy,i));
              igy=0;
            }
            if(igy>ng){
              System.out.println(String.format("igy %d>ng at %d",igy,i));
              igy=ng-1;
            }
            grid[igx][igy]=1;
          }else{
            /*
             * System.out.println(String.format("out of range at %d",igx));
             * System.out.println(String.format("  %d to %d",nx2,nx1));
             * System.out.println(String.format("  %e< %e< %e",ex2,x,ex1));
             * System.out.println(String.format("   %e, %d, %e",dx,igx,xmin));
             */
          }
        }
      }
    }//i

    if(cbInner.isSelected())inner.clear();
    int inc=0;
    for(int ix=0;ix<ng;ix++){
      //search y1,y2
      int iymax=-2*ng;
      int iymin=2*ng;
      for(int iy=0;iy<=ng;iy++){
        if(grid[ix][iy]==1){
          if(iy<iymin)iymin=iy;
          if(iy>iymax)iymax=iy;
        }
      }
      if(iymax==-2*ng || iymin==2*ng){
        myecho("** WARNING **");
        myecho(String.format("  error at %d",ix));
        myecho("Send a mail to nkmrtkhd@gmail.com");
        return;
      }

      //inner area
      if(cbInner.isSelected()){
        int iymid=(iymax+iymin)/2;
        //ymax down
        for(int iy=iymax;iy>iymid;iy--)if(grid[ix][iy]==1 && iy<iymax)iymax=iy;
        //ymin up
        for(int iy=iymin;iy<iymid;iy++)if(grid[ix][iy]==1 && iy>iymin)iymin=iy;

        inner.add(ix);
        inner.add(iymin);
        inner.add(iymax);
      }//end of inner-if

      inc+=(iymax-iymin);
    }//ix

    if(cbConvexHull.isSelected()){
      myecho("-- RESULT with convex-hull --");
    }else{
      myecho("-- RESULT --");
    }
    myecho(String.format("(Lx, Ly) =(%.3e, %.3e)",xmax-xmin,ymax-ymin));
    myecho(String.format("  - generated mesh: %d x %d",ng,ng));
    myecho(String.format("  - unit are=%.5e (=%.3e x %.3e)",dx*dy,dx,dy));
    myecho(String.format("estimate area: %.5e",dx*dy*inc));
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
    //frame.setIconImage(Toolkit.getDefaultToolkit().getImage("icon_confused.gif"));

    ImageIcon icon = new ImageIcon(this.getClass().getResource("/img/icon128.png"));
    ctrlJframe.setIconImage(icon.getImage());
    //window size
    Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
    ctrlJframe.setBounds( 0, 0,1100,600);
    //how to action, when close
    ctrlJframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


    //panel
    JPanel jp=new JPanel();

    openButton=new JButton("Open");
    openButton.addActionListener( this );
    openButton.setFocusable(false);
    openButton.setActionCommand("open");

    calButton=new JButton("Cal.");
    calButton.addActionListener( this );
    calButton.setFocusable(false);

    cbConvexHull =new JCheckBox("Use convex-hull",false);
    cbConvexHull.setFocusable(false);

    cbSort =new JCheckBox("sort data",false);
    cbSort.setFocusable(false);

    cbInner =new JCheckBox("Inner Area",false);
    cbInner.setFocusable(false);


    JLabel lgrid = new JLabel( "Grid Num." );
    spGrid = new JSpinner(new SpinnerNumberModel(500, 1, null, 500));
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
    sp.setPreferredSize(new Dimension(400, 300));

    myCanv=new MyCanvas();
    myCanv.setPreferredSize(new Dimension(400, 400));
    myCanv.setBackground(new Color(200,200,255));
    myCanv.repaint();

    SpringLayout layout = new SpringLayout();
    jp.setLayout(layout);


    layout.putConstraint( SpringLayout.NORTH,openButton, 5,SpringLayout.NORTH, jp);
    layout.putConstraint( SpringLayout.WEST,openButton, 5,SpringLayout.WEST,jp);

    layout.putConstraint( SpringLayout.NORTH,lgrid, 5,SpringLayout.NORTH, openButton);
    layout.putConstraint( SpringLayout.WEST,lgrid, 5,SpringLayout.EAST, openButton);
    layout.putConstraint( SpringLayout.NORTH,spGrid, 5,SpringLayout.NORTH, jp);
    layout.putConstraint( SpringLayout.WEST,spGrid, 2,SpringLayout.EAST, lgrid);

    layout.putConstraint( SpringLayout.NORTH,cbSort, 0,SpringLayout.NORTH, spGrid);
    layout.putConstraint( SpringLayout.WEST,cbSort, 5,SpringLayout.EAST, spGrid);

    layout.putConstraint( SpringLayout.NORTH,cbConvexHull, 0,SpringLayout.SOUTH, cbSort);
    layout.putConstraint( SpringLayout.WEST,cbConvexHull,0,SpringLayout.WEST,cbSort);

    layout.putConstraint( SpringLayout.NORTH,cbInner, 0,SpringLayout.NORTH, cbSort);
    layout.putConstraint( SpringLayout.WEST, cbInner,0,SpringLayout.EAST,cbConvexHull);

    layout.putConstraint( SpringLayout.NORTH,calButton, 5,SpringLayout.NORTH, jp);
    layout.putConstraint( SpringLayout.WEST,calButton, 5,SpringLayout.EAST, cbInner);


    layout.putConstraint( SpringLayout.NORTH,sp, 0,SpringLayout.SOUTH, cbConvexHull);
    layout.putConstraint( SpringLayout.SOUTH,sp, -5,SpringLayout.SOUTH, jp);
    layout.putConstraint( SpringLayout.WEST,sp, 5,SpringLayout.WEST, jp);

    layout.putConstraint( SpringLayout.NORTH,myCanv, 0,SpringLayout.NORTH, sp);
    layout.putConstraint( SpringLayout.SOUTH,myCanv, -5,SpringLayout.SOUTH, jp);
    layout.putConstraint( SpringLayout.WEST,myCanv, 5,SpringLayout.EAST, sp);
    layout.putConstraint( SpringLayout.EAST,myCanv, -5,SpringLayout.EAST, jp);


    JLabel nkmr=new JLabel("Made by nkmrtkhd");
    layout.putConstraint( SpringLayout.NORTH,nkmr, 0,SpringLayout.NORTH, jp);
    layout.putConstraint( SpringLayout.EAST,nkmr, -5,SpringLayout.EAST, jp);

    jp.add(cbInner);
    jp.add(cbSort);
    jp.add(cbConvexHull);
    jp.add(nkmr);
    jp.add(openButton);
    jp.add(calButton);
    jp.add(lgrid);
    jp.add(spGrid);
    jp.add(sp);
    jp.add(myCanv);

    ctrlJframe.add(jp);
    ctrlJframe.setVisible(true);
  }
  private void setLookAndFeel(){
    // Possible Look & Feels
    String mac     = "com.sun.java.swing.plaf.mac.MacLookAndFeel";
    String metal   = "javax.swing.plaf.metal.MetalLookAndFeel";
    String motif   = "com.sun.java.swing.plaf.motif.MotifLookAndFeel";
    String windows = "com.sun.java.swing.plaf.windows.WindowsLookAndFeel";
    String gtk     = "com.sun.java.swing.plaf.gtk.GTKLookAndFeel";
    String nimbus  = "com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel";
    /*
     * //show LF index
     * UIManager.LookAndFeelInfo[] installedLafs = UIManager.getInstalledLookAndFeels();
     * for(int i=0; i<installedLafs.length; i++){
     *   UIManager.LookAndFeelInfo info=installedLafs[i];
     *   System.out.println(info.getName());
     * }
     */
    try{
      UIManager.setLookAndFeel( nimbus );
    }catch( Exception ex ){
      //System.out.println(" Nimbus not available!!");
    }
  }


  /** private class for rendering image*/
  private class MyCanvas extends JPanel{
    public void paint(Graphics g){
      Graphics2D g2 = (Graphics2D)g;

      int w=getWidth();
      int h=getHeight();

      int spc=10;
      if(w<h) spc=w/20;
      else  spc=h/20;

      int width=w*9/10;
      int height=h*9/10;

      g2.clearRect(0, 0, w, h);
      //clear
      g.setColor(new Color(200,200,255));
      g.fill3DRect(0,0,w,h,false);

      g.setColor(new Color(255,255,255));
      g.fill3DRect(spc,spc,w-spc*2,h-spc*2,false);


      double dy=(ymax-ymin);
      double dx=(xmax-xmin);


      g2.setStroke(new BasicStroke(1f)); //線の種類を設定
      //axis
      g.setColor(Color.black);
      g.drawLine(w/2,spc,w/2,h-spc);
      g.drawLine(spc,h/2,w-spc,h/2);

      //draw data points
      if(posIn.size()!=0){
        g.setColor(Color.red);
        int r=7;
        for(int i=0;i<posIn.size()/2;i++){
          int x=  (int)((posIn.get(2*i  )-xmin)/dx*width)+spc;
          int y=h-(int)((posIn.get(2*i+1)-ymin)/dy*height)-spc;
          g.fill3DRect(x-r/2,y-r/2,r,r,false);
        }
      }
      //draw outer line
      g.setColor(Color.green);
      for(int i=0;i<outer.size()/2-1;i++){
        int x =  (int)((outer.get(2*i  )-xmin)*width/dx)+spc;
        int y =h-(int)((outer.get(2*i+1)-ymin)*height/dy)-spc;
        int x1=  (int)((outer.get(2*i+2)-xmin)*width/dx)+spc;
        int y1=h-(int)((outer.get(2*i+3)-ymin)*height/dy)-spc;
        g.drawLine(x,y,x1,y1);
      }
      //draw outer points
      g.setColor(Color.blue);
      for(int i=0;i<outer.size()/2;i++){
        int r=5;
        int x=  (int)((outer.get(2*i  )-xmin)*width/dx)+spc;
        int y=h-(int)((outer.get(2*i+1)-ymin)*height/dy)-spc;
        g.fill3DRect(x-r/2,y-r/2,r,r,false);
      }

      //draw inner
      if(cbInner.isSelected()){
        g.setColor(Color.yellow);
        int ng=((Integer)spGrid.getValue()).intValue();
        for(int i=0;i<inner.size()/3;i++){
          int r=3;
          int x=   (int)((inner.get(3*i  ))*width/ng)+spc;
          int y1=h-(int)((inner.get(3*i+1))*height/ng)-spc;
          int y2=h-(int)((inner.get(3*i+2))*height/ng)-spc;
          g.fill3DRect(x-r/2,y1-r/2,r,r,false);
          g.fill3DRect(x-r/2,y2-r/2,r,r,false);
        }
      }

      //num
      g.setColor(Color.black);
      g2.drawString(String.format("%.2e",xmin),spc,h/2);
      g2.drawString(String.format("%.2e",xmax),w-70,h/2);
      g2.drawString(String.format("%.2e",ymin),w/2,h-spc);
      g2.drawString(String.format("%.2e",ymax),w/2,spc);

    }//paint
  }//end of mycanvas


}//end of this class
