      program make
c     Last modified <2011-08-23 00:15:08 by NAKAMURA Takahide>

      implicit none
      integer i,n

      n=1300
      open(11,file='dat.in')
      write(11,*)n
      do i=1,n
        write(11,'(20e12.4)')cos((i-1)*3.1415d0/dble(n)*2),
     &       sin((i-1)*3.1415d0/dble(n)*2)
      enddo


c-----end of main
      end program make
cccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccc
