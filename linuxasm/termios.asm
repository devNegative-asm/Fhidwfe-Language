
; c_cc characters
VINTR:	equ 0
VQUIT:	equ 1
VERASE:	equ 2
VKILL:	equ 3
VEOF:	equ 4
VTIME:	equ 5
VMIN:	equ 6
VSWTC:	equ 7
VSTART:	equ 8
VSTOP:	equ 9
VSUSP:	equ 10
VEOL:	equ 11
VREPRINT:	equ 12
VDISCARD:	equ 13
VWERASE:	equ 14
VLNEXT:	equ 15
VEOL2:	equ 16

; c_iflag bits
IGNBRK:	equ 0o000001
BRKINT:	equ 0o000002
IGNPAR:	equ 0o000004
PARMRK:	equ 0o000010
INPCK:	equ 0o000020
ISTRIP:	equ 0o000040
INLCR:	equ 0o000100
IGNCR:	equ 0o000200
ICRNL:	equ 0o000400
IUCLC:	equ 0o001000
IXON:	equ 0o002000
IXANY:	equ 0o004000
IXOFF:	equ 0o010000
IMAXBEL:	equ 0o020000
IUTF8:	equ 0o040000

; c_oflag bits
OPOST:	equ 0o000001
OLCUC:	equ 0o000002
ONLCR:	equ 0o000004
OCRNL:	equ 0o000010
ONOCR:	equ 0o000020
ONLRET:	equ 0o000040
OFILL:	equ 0o000100
OFDEL:	equ 0o000200
NLDLY:	equ 0o000400
NL0:	equ 0o000000
NL1:	equ 0o000400
CRDLY:	equ 0o003000
oflag_CR0:	equ 0o000000
oflag_CR1:	equ 0o001000
oflag_CR2:	equ 0o002000
oflag_CR3:	equ 0o003000
TABDLY:	equ 0o014000
TAB0:	equ 0o000000
TAB1:	equ 0o004000
TAB2:	equ 0o010000
TAB3:	equ 0o014000
XTABS:	equ 0o014000
BSDLY:	equ 0o020000
BS0:	equ 0o000000
BS1:	equ 0o020000
VTDLY:	equ 0o040000
VT0:	equ 0o000000
VT1:	equ 0o040000
FFDLY:	equ 0o100000
FF0:	equ 0o000000
FF1:	equ 0o100000

; c_cflag bit meaning 
CBAUD:	equ 0o010017
B0:	equ 0o000000
B50:	equ 0o000001
B75:	equ 0o000002
B110:	equ 0o000003
B134:	equ 0o000004
B150:	equ 0o000005
B200:	equ 0o000006
B300:	equ 0o000007
B600:	equ 0o000010
B1200:	equ 0o000011
B1800:	equ 0o000012
B2400:	equ 0o000013
B4800:	equ 0o000014
B9600:	equ 0o000015
B19200:	equ 0o000016
B38400:	equ 0o000017

CSIZE:	equ 0o000060
CS5:	equ 0o000000
CS6:	equ 0o000020
CS7:	equ 0o000040
CS8:	equ 0o000060
CSTOPB:	equ 0o000100
CREAD:	equ 0o000200
PARENB:	equ 0o000400
PARODD:	equ 0o001000
HUPCL:	equ 0o002000
CLOCAL:	equ 0o004000
CBAUDEX:	equ 0o010000
BOTHER:	equ 0o010000
B57600:	equ 0o010001
B115200:	equ 0o010002
B230400:	equ 0o010003
B460800:	equ 0o010004
B500000:	equ 0o010005
B576000:	equ 0o010006
B921600:	equ 0o010007
B1000000:	equ 0o010010
B1152000:	equ 0o010011
B1500000:	equ 0o010012
B2000000:	equ 0o010013
B2500000:	equ 0o010014
B3000000:	equ 0o010015
B3500000:	equ 0o010016
B4000000:	equ 0o010017
CIBAUD:	equ 0o02003600000  ; input baud rate
CMSPAR:	equ 0o10000000000  ; mark or space (stick) parity
CRTSCTS:	equ 0o20000000000  ; flow control
IBSHIFT:	equ 16            ; Shift from CBAUD to CIBAUD

; c_lflag bits
ISIG:	equ 0o000001
ICANON:	equ 0o000002
XCASE:	equ 0o000004
ECHO:	equ 0o000010
ECHOE:	equ 0o000020
ECHOK:	equ 0o000040
ECHONL:	equ 0o000100
NOFLSH:	equ 0o000200
TOSTOP:	equ 0o000400
ECHOCTL:	equ 0o001000
ECHOPRT:	equ 0o002000
ECHOKE:	equ 0o004000
FLUSHO:	equ 0o010000
PENDIN:	equ 0o040000
IEXTEN:	equ 0o100000
EXTPROC:	equ 0o200000

; tcflow() and TCXONC use these
TCOOFF:	equ 0
TCOON:	equ 1
TCIOFF:	equ 2
TCION:	equ 3

; tcflush() and TCFLSH use these
TCIFLUSH:	equ 0
TCOFLUSH:	equ 1
TCIOFLUSH:	equ 2

; tcsetattr uses these
TCSANOW:	equ 0
TCSADRAIN:	equ 1
TCSAFLUSH:	equ 2


;;;;;;;;;;;;;;;;;
;FCNTL constants;
;;;;;;;;;;;;;;;;;

O_ACCMODE:	equ       0o0000003
O_RDONLY:	equ        0o0000000
O_WRONLY:	equ        0o0000001
O_RDWR:	equ          0o0000002
O_CREAT:	equ         0o0000100        ;  not fcntl 
O_EXCL:	equ          0o0000200        ;  not fcntl 
O_NOCTTY:	equ        0o0000400        ;  not fcntl 
O_TRUNC:	equ         0o0001000        ;  not fcntl 
O_APPEND:	equ        0o0002000
O_NONBLOCK:	equ      0o0004000
O_DSYNC:	equ         0o0010000        ;  used to be O_SYNC, see below 
FASYNC:	equ          0o0020000        ;  fcntl, for BSD compatibility 
O_DIRECT:	equ        0o0040000        ;  direct disk access hint 
O_LARGEFILE:	equ     0o0100000
O_DIRECTORY:	equ     0o0200000        ;  must be a directory 
O_NOFOLLOW:	equ      0o0400000        ;  don't follow links 
O_NOATIME:	equ       0o1000000
O_CLOEXEC:	equ       0o2000000        ;  set close_on_exec 

__O_SYNC:	equ        0o4000000
O_SYNC:	equ          (__O_SYNC|O_DSYNC)

O_PATH:	equ          0o10000000

__O_TMPFILE:	equ     0o20000000

;  a horrid kludge trying to make sure that this will fail on old kernels 
O_TMPFILE:	equ (__O_TMPFILE | O_DIRECTORY)
O_TMPFILE_MASK:	equ (__O_TMPFILE | O_DIRECTORY | O_CREAT)      

O_NDELAY:	equ        O_NONBLOCK

F_DUPFD:	equ         0       ;  dup 
F_GETFD:	equ         1       ;  get close_on_exec 
F_SETFD:	equ         2       ;  set/clear close_on_exec 
F_GETFL:	equ         3       ;  get file->f_flags 
F_SETFL:	equ         4       ;  set file->f_flags 
F_GETLK:	equ         5
F_SETLK:	equ         6
F_SETLKW:	equ        7
F_SETOWN:	equ        8       ;  for sockets. 
F_GETOWN:	equ        9       ;  for sockets. 
F_SETSIG:	equ        10      ;  for sockets. 
F_GETSIG:	equ        11      ;  for sockets. 

F_GETLK64:	equ       12      ;   using 'struct flock64' 
F_SETLK64:	equ       13
F_SETLKW64:	equ      14

F_SETOWN_EX:	equ     15
F_GETOWN_EX:	equ     16

F_GETOWNER_UIDS:	equ 17


F_OFD_GETLK:	equ     36
F_OFD_SETLK:	equ     37
F_OFD_SETLKW:	equ    38

F_OWNER_TID:	equ     0
F_OWNER_PID:	equ     1
F_OWNER_PGRP:	equ    2


;  for F_[GET|SET]FL 
FD_CLOEXEC:	equ      1       ;  actually anything with low bit set goes 

;  for posix fcntl() and lockf() 
F_RDLCK:	equ         0
F_WRLCK:	equ         1
F_UNLCK:	equ         2

;  for old implementation of bsd flock () 
F_EXLCK:	equ         4       ;  or 3 
F_SHLCK:	equ         8       ;  or 4 

;  operations for bsd flock(), also used by the kernel implementation 
LOCK_SH:	equ         1       ;  shared lock 
LOCK_EX:	equ         2       ;  exclusive lock 
LOCK_NB:	equ         4       ;  or'd with one of the above to prevent blocking
LOCK_UN:	equ         8       ;  remove lock 

LOCK_MAND:	equ       32      ;  This is a mandatory flock ... 
LOCK_READ:	equ       64      ;  which allows concurrent read operations 
LOCK_WRITE:	equ      128     ;  which allows concurrent write operations 
LOCK_RW:	equ         192     ;  which allows concurrent read & write ops 