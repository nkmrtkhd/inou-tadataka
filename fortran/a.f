      program make
c     Last modified <2011-08-26 10:23:18 by NAKAMURA Takahide>

      implicit none
      integer i,n

      n=1300
      open(11,file='circle.in')
      open(22,file='ellipse.in')
      do i=1,n
        write(11,'(20e12.4)')cos((i-1)*3.1415d0/dble(n)*2),
     &       sin((i-1)*3.1415d0/dble(n)*2)
        write(22,'(20e12.4)')cos((i-1)*3.1415d0/dble(n)*2)*2d0,
     &       sin((i-1)*3.1415d0/dble(n)*2)*3d0
      enddo


c-----end of main
      end program make
cccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccc
