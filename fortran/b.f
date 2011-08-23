      program make
c     Last modified <2011-08-23 00:45:52 by NAKAMURA Takahide>

      implicit none
      integer,parameter::nmax=5000
      integer,parameter::ng=100*2
      real*8 r(2,nmax),xmax,xmin,ymax,ymin,dx,dy,x,y,y1,y2,area
      integer i,n,ix,iy,inc

      xmax=-100
      xmin=100
      ymax=-100
      ymin=100
      open(22,file='dat.in')
      read(22,*) n
      do i=1,n
        read(22,*)r(1:2,i)
        if(r(1,i).gt.xmax)xmax=r(1,i)
        if(r(1,i).lt.xmin)xmin=r(1,i)
        if(r(2,i).gt.ymax)ymax=r(2,i)
        if(r(2,i).lt.ymin)ymin=r(2,i)
      enddo
      close(22)


      dx=(xmax-xmin)/ng
      dy=(ymax-ymin)/ng
      print*,'xmax, xmin',xmax,xmin
      print*,'ymax, ymin',ymax,ymin
      print*,'dx,dy',dx,dy
      inc=0
      area=0d0
      do ix=0,ng
        x=xmin+dx*ix
        call setRegion(x,y1,y2,r,nmax,n,dx,dy)
c-------cal 1
        do iy=0,ng
          y=ymin+dy*iy
          if(y1.le.y .and. y.le.y2)then
            inc=inc+1
          endif
        enddo
c-------cal 2
        area=area+(y2-y1)*dx
      enddo

      print*,'unit Area= ',dx*dy
      print*,'hit unit= ',inc
      print*,'SO, hit Area= ',dx*dy*inc
      print*,'Area= ',area


c-----end of main
      end program make
cccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccc
      subroutine  setRegion(x,y1,y2,r,nmax,n,dx,dy)
      implicit none
      integer n,nmax
      real*8 x,r(2,nmax),y1,y2,dx,dy

      integer i

      y1=1d10
      y2=-1d10
      do i=1,n
        if(abs(x-r(1,i)).le.dx)then
          if(r(2,i).lt.y1)y1=r(2,i)
          if(r(2,i).gt.y2)y2=r(2,i)
        endif
      enddo
      if(y1.eq.1d10 .or. y2.eq.1d10)stop'too big ng'

      end
