%include "linuxasm/unistd_64.asm"
%include "linuxasm/termios.asm"
extern error
extern Fwf_internal_main
global unix_syscall
global findlsb256
global terminal_mode_raw
global terminal_mode_default
section .data

Fwf_internal_syscall_argument_check:
	db 3 ; read
	db 3 ; write
	db 3 ; open
	db 1 ; close
	db 2 ; newstat
	db 2 ; newfstat
	db 2 ; newlstat
	db 3 ; poll
	db 3 ; lseek
	db 6 ; mmap
	db 3 ; mprotect
	db 2 ; munmap
	db 1 ; brk
	db 4 ; rt_sigaction
	db 4 ; rt_sigprocmask
	db 0 ; rt_sigreturn
	db 3 ; ioctl
	db 4 ; pread64
	db 4 ; pwrite64
	db 3 ; readv
	db 3 ; writev
	db 2 ; access
	db 1 ; pipe
	db 5 ; select
	db 0 ; sched_yield
	db 5 ; mremap
	db 3 ; msync
	db 3 ; mincore
	db 3 ; madvise
	db 3 ; shmget
	db 3 ; shmat
	db 3 ; shmctl
	db 1 ; dup
	db 2 ; dup2
	db 0 ; pause
	db 2 ; nanosleep
	db 2 ; getitimer
	db 1 ; alarm
	db 3 ; setitimer
	db 0 ; getpid
	db 4 ; sendfile64
	db 3 ; socket
	db 3 ; connect
	db 3 ; accept
	db 6 ; sendto
	db 6 ; recvfrom
	db 3 ; sendmsg
	db 3 ; recvmsg
	db 2 ; shutdown
	db 3 ; bind
	db 2 ; listen
	db 3 ; getsockname
	db 3 ; getpeername
	db 4 ; socketpair
	db 5 ; setsockopt
	db 5 ; getsockopt
	db 5 ; clone
	db 0 ; fork
	db 0 ; vfork
	db 3 ; execve
	db 1 ; exit
	db 4 ; wait4
	db 2 ; kill
	db 1 ; newuname
	db 3 ; semget
	db 3 ; semop
	db 4 ; semctl
	db 1 ; shmdt
	db 2 ; msgget
	db 4 ; msgsnd
	db 5 ; msgrcv
	db 3 ; msgctl
	db 3 ; fcntl
	db 2 ; flock
	db 1 ; fsync
	db 1 ; fdatasync
	db 2 ; truncate
	db 2 ; ftruncate
	db 3 ; getdents
	db 2 ; getcwd
	db 1 ; chdir
	db 1 ; fchdir
	db 2 ; rename
	db 2 ; mkdir
	db 1 ; rmdir
	db 2 ; creat
	db 2 ; link
	db 1 ; unlink
	db 2 ; symlink
	db 3 ; readlink
	db 2 ; chmod
	db 2 ; fchmod
	db 3 ; chown
	db 3 ; fchown
	db 3 ; lchown
	db 1 ; umask
	db 2 ; gettimeofday
	db 2 ; getrlimit
	db 2 ; getrusage
	db 1 ; sysinfo

Fwf_internal_syscall_argument_check_end:
Fwf_internal_syscall_error:
	db "incorrect syscall usage", 0


Fwf_internal_termios_default:
.c_iflag:
	dd 0;
.c_oflag:
	dd 0;
.c_cflag:
	dd 0;
.c_lflag:
	dd 0;
.c_cc: ; c_cc[32]
	dq 0
	dq 0
	dq 0
	dq 0
.garbage: ;stuff I don't care about, but the syscall might write here
	dq 0
	dq 0
	dq 0
	dq 0
	dq 0
	dq 0
	dq 0
	dq 0

Fwf_internal_termios_raw:
.c_iflag:
	dd 0;
.c_oflag:
	dd 0;
.c_cflag:
	dd 0;
.c_lflag:
	dd 0;
.c_cc: ; c_cc[32]
	dq 0
	dq 0
	dq 0
	dq 0
.garbage: ;stuff I don't care about, but the syscall might write here
	dq 0
	dq 0
	dq 0
	dq 0
	dq 0
	dq 0
	dq 0
	dq 0

TCGETS: equ 0x5401
TCSETS: equ 0x5402

section .text
global _start
_start:
	mov eax, __sys_ioctl
	xor edi, edi
	mov esi, TCGETS
	lea rdx, [Fwf_internal_termios_raw]
	syscall

	mov eax, __sys_ioctl
	xor edi, edi
	mov esi, TCGETS
	lea rdx, [Fwf_internal_termios_default]
	syscall

	;edit the bits of the raw termios struct
	
	;INPUT FLAGS

	;disable bits
	and word [Fwf_internal_termios_raw.c_iflag], ~(ISTRIP|INLCR)
	;enable bits
	or word [Fwf_internal_termios_raw.c_iflag], IGNBRK|ICRNL


	;OUTPUT FLAGS
	and word [Fwf_internal_termios_raw.c_oflag], ~(OPOST|ONLCR|OCRNL)

	;lflags
	and word [Fwf_internal_termios_raw.c_lflag], ~(ISIG|ICANON|ECHO|IEXTEN)




	call Fwf_internal_main
	mov rdi, rax
	mov rax, __sys_exit
	syscall

terminal_mode_raw:
	mov eax, __sys_ioctl
	xor edi, edi
	mov esi, TCSETS
	lea rdx, [Fwf_internal_termios_raw]
	syscall

	mov eax, __sys_fcntl
	mov edi, 0
	mov esi, F_GETFL
	syscall

	mov eax, __sys_fcntl
	mov rdx, rax
	xor edi, edi
	mov esi, F_SETFL
	or rdx, O_NONBLOCK
	syscall

	ret

terminal_mode_default:
	mov eax, __sys_ioctl
	xor edi, edi
	mov esi, TCSETS
	lea rdx, [Fwf_internal_termios_default]
	syscall

	mov eax, __sys_fcntl
	xor edi, edi
	mov esi, F_GETFL
	syscall

	mov eax, __sys_fcntl
	mov rdx, rax
	xor edi,edi
	mov esi, F_SETFL
	and rdx, ~O_NONBLOCK
	syscall

	ret

unix_syscall:

	; rax = syscall id
	; rbx = input object

	;stack contains {retaddr} {prev_rbp}
	pop rax
	xchg [rsp], rbp
	xchg [rsp + 8], rax


	; if we are passed nullptr for input object, treat that like size of 0
	mov rcx, 0
	test rbp,rbp
	cmovnz rcx, [rbp - 8]

	;if rax is out of range, fail with error

	test rax, rax
	js Fwf_internal_syscall_exit_error
	cmp rax, Fwf_internal_syscall_argument_check_end - Fwf_internal_syscall_argument_check
	jnc Fwf_internal_syscall_exit_error

	;if the list provided is not the right length, fail with error

	shr rcx, 3
	mov sil, [Fwf_internal_syscall_argument_check + rax]
	and rsi, 0xff
	cmp rcx, rsi
	jne Fwf_internal_syscall_exit_error

	;input object matches syscall size
	;move arguments from [rbp] into correct registers

	test rcx, rcx
	jz .noarg
	cmp rcx, 1
	jz .onearg
	cmp rcx, 2
	jz .twoarg
	cmp rcx, 3
	jz .threearg
	cmp rcx, 4
	jz .fourarg
	cmp rcx, 5
	jz .fivearg

	mov r9, [rbp+0x28]
.fivearg:
	mov r8, [rbp+0x20]
.fourarg:
	mov r10, [rbp+0x18]
.threearg:
	mov rdx, [rbp+0x10]
.twoarg:
	mov rsi, [rbp+0x08]
.onearg:
	mov rdi, [rbp]
.noarg:
	syscall
	pop rbp
	ret

Fwf_internal_syscall_exit_error:
	lea rax, [Fwf_internal_syscall_error]
	push rax
	call error

findlsb256:
	pop rdi
	xchg [rsp], rdi

.cVersion:
	mov rdx, -1
	bsf rax, [rdi]
	jz .test2
	ret

.test2:
	bsf rax, [rdi+0x08]
	jz .test3
	add rax, 0x40
	ret

.test3:
	bsf rax, [rdi+0x10]
	jz .test4
	add rax, 0x80
	ret

.test4:
	bsf rax, [rdi+0x18]
	jz .n1
	add rax, 0xc0
	ret

.n1:
	mov rax, -1
	ret

global bs1


bs1:
	mov rdx, -1
	bsf rax, [rdi]
	jz .test2
	ret

.test2:
	bsf rax, [rdi+0x08]
	jz .test3
	add rax, 0x40
	ret

.test3:
	bsf rax, [rdi+0x10]
	jz .test4
	add rax, 0x80
	ret

.test4:
	bsf rax, [rdi+0x18]
	jz .n1
	add rax, 0xc0
	ret

.n1:
	mov rax, -1
	ret
