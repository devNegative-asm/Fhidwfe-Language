;Included for TASM Compatibility
;-------------------------------
;equ:	EQU .equ
;EQU:	EQU .equ
;end:	EQU .end
;END:	EQU .end

;System Variable Equates
;------------------------------
XSCLt:				EQU 2
YSCLt:				EQU 3
XMINt:				EQU 0Ah
XMAXt:				EQU 0Bh
YMINt:				EQU 0Ch
YMAXt:				EQU 0Dh
TMINt:				EQU 0Eh
TMAXt:				EQU 0Fh
THETMINt:				EQU 10h
THETMAXt:				EQU 11h
TBLMINt:				EQU 1Ah
PLOTSTARTt:				EQU 1Bh
NMAXt:				EQU 1Dh
NMINt:				EQU 1Fh
TBLSTEPt:				EQU 21h
TSTEPt:				EQU 22h
THETSTEPt:				EQU 23h
DELTAXt:				EQU 26h
DELTAYt:				EQU 27h
XFACTt:				EQU 28h
YFACTt:				EQU 29h
FINNt:				EQU 2Bh
FINIt:				EQU 2Ch
FINPVt:				EQU 2Dh
FINPMTt:				EQU 2Eh
FINFVt:				EQU 2Fh
FINPYt:				EQU 30h
FINCYt:				EQU 31h
PLOTSTEPt:				EQU 34h
XRESt:				EQU 36h

;Run indicators
;------------------------------------
busyPause:				EQU 10101010b
busyNormal:				EQU 11110000b

;Banked Call Equates/Macros
;-----------------------------------------------
rBR_CALL:				EQU 28h
BRT_JUMP0:				EQU 50h

;Common Restart Routines
;------------------------------
rOP1TOOP2:				EQU 08h
rFINDSYM:				EQU 10h
rPUSHREALO1:				EQU 18h
rMOV9TOOP1:				EQU 20h
rFPADD:				EQU 30h

;Error Handler Equates/Macros
;-------------------------------------------
APP_PUSH_ERRORH:				EQU 59h
APP_POP_ERRORH:				EQU 5Ch

;Page 1Bh ROM Calls
;--------------------------------
_JErrorNo:				EQU 4000h
_FontHook:				EQU 4003h
_call_character_hook:				EQU 4006h ;calls character(localize) hook
_ldHLind:				EQU 4009h ;l=a=(hl),h=(hl+1)
_CpHLDE:				EQU 400Ch
_DivHLBy10:				EQU 400Fh
_DivHLByA:				EQU 4012h
_kdbScan:				EQU 4015h
_GetCSC:				EQU 4018h ;key board scan codes
_coorMon:				EQU 401Bh ;
_Mon:				EQU 401Eh ;system monitor, customized through the context vectors
_monForceKey:				EQU 4021h ;
_sendKPress:				EQU 4024h
_JForceCmdNoChar:				EQU 4027h
_JForceCmd:				EQU 402Ah ;
_sysErrHandler:				EQU 402Dh ;loads error context? sp=(onSP)
_newContext:				EQU 4030h ;(kbdKey)=0, loads context, restores page in 4000h-7fffh
_newContext0:				EQU 4033h ;loads context
_cxPutAway:				EQU 4036h ;
_cxPutAway2:				EQU 4039h ;same but also does a B_CALL CanAlphIns
_cxPPutAway:				EQU 403Ch
_cxSizeWind:				EQU 403Fh
_cxErrEP:				EQU 4042h
_cxMain:				EQU 4045h
_monErrHand:				EQU 4048h ;installs errorhandler to start of ROM call, loads error context, jumps to mon
_AppInit:				EQU 404Bh ;loads vector data at hl to cxMain and the rest of the vectors
_resetRam:				EQU 404Eh
_lcd_busy:				EQU 4051h ;wait till bit 1 of port 2 is set
_Min:				EQU 4054h ;op1 = lowest number between op1 and op2
_Max:				EQU 4057h ;op1 = highest number between op1 and op2 
_Trunc:				EQU 4060h
_InvSub:				EQU 4063h
_Times2:				EQU 4066h
_Plus1:				EQU 4069h ;op1=op1+1
_Minus1:				EQU 406Ch
_FPSub:				EQU 406Fh
_FPAdd:				EQU 4072h
_DToR:				EQU 4075h
_RToD:				EQU 4078h
_Cube:				EQU 407Bh
_TimesPt5:				EQU 407Eh
_FPSquare:				EQU 4081h
_FPMult:				EQU 4084h ;op1=op1*op2
_LJRND:				EQU 4087h ;adjusts op1 if 0s precede the actual number... rounding? when
_InvOP1SC:				EQU 408Ah
_InvOP1S:				EQU 408Dh
_InvOP2S:				EQU 4090h
_frac:				EQU 4093h
_fprecip:				EQU 4096h
_fpdiv:				EQU 4099h
_SqRoot:				EQU 409Ch
_RndGuard:				EQU 409Fh
_RnFx:				EQU 40A2h
_int:				EQU 40A5h
_Round:				EQU 40A8h
_LnX:				EQU 40ABh
_LogX:				EQU 40AEh
_LJNORND:				EQU 40B1h ;like _LJRND but no rounding
_EToX:				EQU 40B4h
_TenX:				EQU 40B7h
_SinCosRad:				EQU 40BAh
_Sin:				EQU 40BDh
_cos:				EQU 40C0h
_Tan:				EQU 40C3h
_SinHCosH:				EQU 40C6h
_TanH:				EQU 40C9h
_cosh:				EQU 40CCh
_SinH:				EQU 40CFh
_ACosRad:				EQU 40D2h
_ATanRad:				EQU 40D5h
_ATan2Rad:				EQU 40D8h
_ASinRad:				EQU 40DBh
_ACos:				EQU 40DEh
_ATan:				EQU 40E1h
_ASin:				EQU 40E4h
_ATan2:				EQU 40E7h
_ATanH:				EQU 40EAh
_ASinH:				EQU 40EDh
_ACosH:				EQU 40F0h
_PtoR:				EQU 40F3h
_RToP:				EQU 40F6h
_HLTimes9:				EQU 40F9h
_CkOP1Cplx:				EQU 40FCh
_CkOP1Real:				EQU 40FFh
_Angle:				EQU 4102h
_COP1Set0:				EQU 4105h
_CpOP4OP3:				EQU 4108h
_Mov9OP2Cp:				EQU 410Bh
_AbsO1O2Cp:				EQU 410Eh
_cpop1op2:				EQU 4111h
_OP3ToOP4:				EQU 4114h
_OP1ToOP4:				EQU 4117h
_OP2ToOP4:				EQU 411Ah
_OP4ToOP2:				EQU 411Dh
_OP1ToOP3:				EQU 4123h
_OP5ToOP2:				EQU 4126h
_OP5ToOP6:				EQU 4129h
_OP5ToOP4:				EQU 412Ch
_OP1ToOP2:				EQU 412Fh
_OP6ToOP2:				EQU 4132h
_OP6ToOP1:				EQU 4135h
_OP4ToOP1:				EQU 4138h
_OP5ToOP1:				EQU 413Bh
_OP3ToOP1:				EQU 413Eh
_OP6ToOP5:				EQU 4141h
_OP4ToOP5:				EQU 4144h
_OP3ToOP5:				EQU 4147h
_OP2ToOP5:				EQU 414Ah
_OP2ToOP6:				EQU 414Dh
_OP1ToOP6:				EQU 4150h
_OP1ToOP5:				EQU 4153h
_OP2ToOP1:				EQU 4156h
_Mov11B:				EQU 4159h
_Mov10B:				EQU 415Ch
_Mov9B:				EQU 415Fh
_mov9B2:				EQU 4162h ;points to _mov9B
_Mov8B:				EQU 4165h
_Mov7B:				EQU 4168h
_Mov7B2:				EQU 416Bh ;same pointer as _Mov7B
_OP2ToOP3:				EQU 416Eh
_OP4ToOP3:				EQU 4171h
_OP5ToOP3:				EQU 4174h
_OP4ToOP6:				EQU 4177h
_Mov9ToOP1:				EQU 417Ah
_Mov9OP1OP2:				EQU 417Dh
_Mov9ToOP2:				EQU 4180h
_MovFrOP1:				EQU 4183h
_OP4Set1:				EQU 4186h
_OP3Set1:				EQU 4189h
_OP2Set8:				EQU 418Ch
_OP2Set5:				EQU 418Fh
_OP2SetA:				EQU 4192h
_OP2Set4:				EQU 4195h
_OP2Set3:				EQU 4198h
_OP1Set1:				EQU 419Bh
_OP1Set4:				EQU 419Eh
_OP1Set3:				EQU 41A1h
_OP3Set2:				EQU 41A4h
_OP1Set2:				EQU 41A7h
_OP2Set2:				EQU 41AAh
_OP2Set1:				EQU 41ADh
_Zero16D:				EQU 41B0h
_OP5Set0:				EQU 41B3h
_OP4Set0:				EQU 41B6h
_OP3Set0:				EQU 41B9h
_OP2Set0:				EQU 41BCh
_OP1Set0:				EQU 41BFh
_OPSet0:				EQU 41C2h ;hl = location to write floating point 0
_ZeroOP1:				EQU 41C5h
_ZeroOP2:				EQU 41C8h
_ZeroOP3:				EQU 41CBh
_ZeroOP:				EQU 41CEh
_ClrLp:				EQU 41D1h
_ShRAcc:				EQU 41D4h ;move high nibble in a to low nibble
_ShLAcc:				EQU 41D7h ;move low nibble in a to high nibble
_ShR18:				EQU 41DAh ;insert a 0 nibble at high nibble of (hl), shift 9 bytes 1 nibble to right
_SHR18A:				EQU 41DDh ;insert low nibble in a at high nibble of (hl), shift 9 bytes 1 nibble to right
_SHR16:				EQU 41E0h ;insert a 0 nibble at highnibble of (hl), shift 8 bytes 1 nibble to right
_SHR14:				EQU 41E3h ;insert low nibble in a at high nibble of (hl), shift 7 bytes 1 nibble to right
_SHL16:				EQU 41E6h ;insert nibble of 0 in low nibble of (hl), shift 8 bytes (before and including (hl)) to the left 1 nibble
_SHL14:				EQU 41E9h ;insert low nibble of a in low nibble of (hl), shift 7 bytes (before and including (hl)) to the left 1 nibble
_SRDO1:				EQU 41ECh
_SHRDRND:				EQU 41EFh
_MANTPA:				EQU 41F2h ;adds the value of a to hl which points to the end of the bcd 7 bytes long
_ADDPROP:				EQU 41F5h ;adds the value of a to hl which points to the end of the bcd (b bytes long)
_ADDPROPLP:				EQU 41F8h ;adds the value of a and carry to hl which points to the end of the bcd (b bytes long)
_ADD16D:				EQU 41FBh ;adds the bcd numbers at (hl-7) and (de-7)
_ADD14D:				EQU 41FEh ;adds the bcd numbers at (hl-6) and (de-6)
_SUB16D:				EQU 4201h ;subtracts bcd numbers at (hl-7) and (de-7)
_SUB14D:				EQU 4204h ;subtracts bcd numbers at (hl-6) and (de-6)
_OP2ExOP6:				EQU 4207h
_OP5ExOP6:				EQU 420Ah
_OP1ExOP5:				EQU 420Dh
_OP1ExOP6:				EQU 4210h
_OP2ExOP4:				EQU 4213h
_OP2ExOP5:				EQU 4216h
_OP1ExOP3:				EQU 4219h
_OP1ExOP4:				EQU 421Ch
_OP1ExOP2:				EQU 421Fh
_ExLp:				EQU 4222h
_CkOP1C0:				EQU 4225h
_CkOP1FP0:				EQU 4228h
_CkOP2FP0:				EQU 422Bh
_PosNo0Int:				EQU 422Eh
_CKPosInt:				EQU 4231h
_CKInt:				EQU 4234h
_CKOdd:				EQU 4237h
_CKOP1M:				EQU 423Ah
_GETCONOP1:				EQU 423Dh ;a=0 opX=57.29577951308232 (1 radian in degrees)
_GETCONOP2:				EQU 4240h ;a=1 opX=1.570796326794897 (90 deg = pi/2)
				  ;a=2 opX=.7853981633974483 (45 deg = pi/4)
				  ;a=3 opX=.4342944819032518 (log e)
				  ;a=4 opX=3.141592653589800 (pi)
				  ;a=5 opX=.0174532925199433 (pi/180 = 1 degree in radians)
				  ;a=6 opX=2.302585092994046 (ln 10)
_PIDIV2:				EQU 4243h ;not code, but a pointer to:	DB 80h,15h,70h,79h,63h,26h,79h,48h,97h
_PIDIV4:				EQU 4246h ;				DB 7fh,78h,53h,98h,16h,33h,97h,44h,83h
_PItimes2:				EQU 4249h ;not code, but a pointer to a 2*pi in non-OP format (no exponent byte)
_PI:				EQU 424Ch ;not code, but a pointer to a pi in non-OP format (no exponent byte)
_ExpToHex:				EQU 424Fh
_OP1ExpToDec:				EQU 4252h
_ckop2pos:				EQU 4255h
_CkOP1Pos:				EQU 4258h
_ClrOP2S:				EQU 425Bh
_ClrOP1S:				EQU 425Eh
_FDIV100:				EQU 4261h ;op1=op1/100
_FDIV10:				EQU 4264h ;op1=op1/10
_DecO1Exp:				EQU 4267h ;decrease exponent by 1, this can go from 0 to FF
_INCO1EXP:				EQU 426Ah ;op1=op1*10
_INCEXP:				EQU 426Dh ;hl points to the floating point's exponent to be multiplied by 10
_CkValidNum:				EQU 4270h
_GETEXP:				EQU 4273h ;a=OP1's exponent, carry set if negative exponent, Z if e0
_HTimesL:				EQU 4276h
_EOP1NotReal:				EQU 4279h
_ThetaName:				EQU 427Ch
_RName:				EQU 427Fh
_REGEQNAME:				EQU 4282h
_RECURNNAME:				EQU 4285h
_XName:				EQU 4288h
_YName:				EQU 428Bh
_TName:				EQU 428Eh
_REALNAME:				EQU 4291h
_SETesTOfps:				EQU 4294h ;moves the word at fps to es
_markTableDirty:				EQU 4297h ;looks up table variable and marks VAT entry as "dirty" or selected
_OP1MOP2EXP:				EQU 429Ah ;op1's exponent = op1's expoent - op2's exponent
_OP1EXPMinusE:				EQU 429Dh ;a=(op1+1)-e
_CHKERRBREAK:				EQU 42A0h
_isA2ByteTok:				EQU 42A3h
_GETLASTENTRY:				EQU 42A6h
_GETLASTENTRYPTR:				EQU 42A9h
_REGCLRCHNG:				EQU 42ACh
_RESETWINTOP:				EQU 42AFh ;takes into account grfsplitoverride and grfsplit flags
_SetYUp:				EQU 42B2h ;loads 7 to port 10... what does this do?
_SetXUp:				EQU 42B5h ;loads 5 to port 10...
_ISO1NONTLSTorPROG:				EQU 42B8h ;checks if op1 contains a list, program, group, or appvar obj name
_ISO1NONTEMPLST:				EQU 42BBh ;checks if op1 contains a list (why would op1+1 contain 01, or 0d for a var name?)
_IS_A_LSTorCLST:				EQU 42BEh ;checks if a = 1 or 0Dh
_CHK_HL_999:				EQU 42C1h ;returns nc if less than 999, throws invalid dim error if greater than or:				EQUal to 999
_equ_or_newequ:				EQU 42C4h
_errd_op1notpos:				EQU 42C7h
_ErrD_OP1Not_R:				EQU 42CAh
_ErrD_OP1NotPosInt:				EQU 42CDh
_ErrD_OP1_LE_0:				EQU 42D0h
_ErrD_OP1_0:				EQU 42D3h
_FINDSYM_GET_SIZE:				EQU 42D6h ;like findsym, but on output hl is the size of the variable
_STO_STATVAR:				EQU 42D9h
_Rcl_StatVar:				EQU 42DCh
_CkOP2Real:				EQU 42DFh
_GET_X_INDIRECT:				EQU 42E2h ;whatever this is, it uses the imathptrX locations
_MemChk:				EQU 42E5h
_CMPPRGNAMLEN1:				EQU 42E8h ;gets variable name length from HL
_CMPPRGNAMLEN:				EQU 42EBh ;gets variable name length from OP1
_FINDPROGSYM:				EQU 42EEh ;find the program whose name is in op1 (see chkfindsym in SDK)
_ChkFindSym:				EQU 42F1h
_FindSym:				EQU 42F4h
_InsertMem:				EQU 42F7h
_INSERTMEMA:				EQU 42FAh ;not sure how this differs from insertmem
_EnoughMem:				EQU 42FDh
_CMPMEMNEED:				EQU 4300h
_CREATEPVAR4:				EQU 4303h
_CREATEPVAR3:				EQU 4306h
_CREATEVAR3:				EQU 4309h
_CreateCplx:				EQU 430Ch
_CreateReal:				EQU 430Fh
_CreateTempRList:				EQU 4312h
_CreateRList:				EQU 4315h
_CREATETCLIST:				EQU 4318h
_CreateCList:				EQU 431Bh
_CreateTempRMat:				EQU 431Eh
_CreateRMat:				EQU 4321h
_CreateTempString:				EQU 4324h
_CreateStrng:				EQU 4327h
_Create0Equ:				EQU 432Ah
_CreateTempEqu:				EQU 432Dh
_CreateEqu:				EQU 4330h
_CreatePict:				EQU 4333h
_CreateGDB:				EQU 4336h
_CreateProg:				EQU 4339h
_CHKDEL:				EQU 433Ch
_CHKDELA:				EQU 433Fh
_ADJPARSER:				EQU 4342h
_ADJMATH:				EQU 4345h
_ADJM7:				EQU 4348h
_DELMEMA:				EQU 434Bh
_GET_FORM_NUM:				EQU 434Eh
_DelVar:				EQU 4351h
_DELVARIO:				EQU 4354h
_DelMem:				EQU 4357h
_DELVAR3D:				EQU 435Ah
_DELVAR3C:				EQU 435Dh
_DELVAR3DC:				EQU 4360h ;may be incorrect
_IsFixedName:				EQU 4363h
_DelVarEntry:				EQU 4366h
_DataSizeA:				EQU 4369h
_DataSize:				EQU 436Ch
_POPMCPLXO1:				EQU 436Fh
_POPMCPLX:				EQU 4372h
_MOVCPLX:				EQU 4375h
_popOP5:				EQU 4378h
_popOP3:				EQU 437Bh
_popOP1:				EQU 437Eh
_PopRealO6:				EQU 4381h
_PopRealO5:				EQU 4384h
_PopRealO4:				EQU 4387h
_PopRealO3:				EQU 438Ah
_PopRealO2:				EQU 438Dh
_PopRealO1:				EQU 4390h
_PopReal:				EQU 4393h
_FPOPCPLX:				EQU 4396h
_FPOPREAL:				EQU 4399h
_FPOPFPS:				EQU 439Ch
_DeallocFPS:				EQU 439Fh
_DeallocFPS1:				EQU 43A2h
_AllocFPS:				EQU 43A5h
_AllocFPS1:				EQU 43A8h
_PushRealO6:				EQU 43ABh
_PushRealO5:				EQU 43AEh
_PushRealO4:				EQU 43B1h
_PushRealO3:				EQU 43B4h
_PushRealO2:				EQU 43B7h
_PushRealO1:				EQU 43BAh
_PushReal:				EQU 43BDh
_PushOP5:				EQU 43C0h
_PushOP3:				EQU 43C3h
_PUSHMCPLXO3:				EQU 43C6h
_PushOP1:				EQU 43C9h
_PUSHMCPLXO1:				EQU 43CCh
_PUSHMCPLX:				EQU 43CFh
_ExMCplxO1:				EQU 43D2h
_Exch9:				EQU 43D5h
_CpyTo1FPS11:				EQU 43D8h
_CpyTo2FPS5:				EQU 43DBh
_CpyTo1FPS5:				EQU 43DEh
_CpyTo2FPS6:				EQU 43E1h
_CpyTo1FPS6:				EQU 43E4h
_CpyTo2FPS7:				EQU 43E7h
_CpyTo1FPS7:				EQU 43EAh
_CpyTo1FPS8:				EQU 43EDh
_CpyTo2FPS8:				EQU 43F0h
_CpyTo1FPS10:				EQU 43F3h
_CpyTo1FPS9:				EQU 43F6h
_CpyTo2FPS4:				EQU 43F9h
_CpyTo6FPS3:				EQU 43FCh
_CpyTo6FPS2:				EQU 43FFh
_CpyTo2FPS3:				EQU 4402h
_CPYCTO1FPS3:				EQU 4405h
_CpyTo1FPS3:				EQU 4408h
_CPYFPS3:				EQU 440Bh
_CpyTo1FPS4:				EQU 440Eh
_CpyTo3FPS2:				EQU 4411h
_CpyTo5FPST:				EQU 4414h
_CpyTo6FPST:				EQU 4417h
_CpyTo4FPST:				EQU 441Ah
_CpyTo3FPST:				EQU 441Dh
_CpyTo2FPST:				EQU 4420h
_CpyTo1FPST:				EQU 4423h
_CPYFPST:				EQU 4426h
_CpyStack:				EQU 4429h
_CpyTo3FPS1:				EQU 442Ch
_CpyTo2FPS1:				EQU 442Fh
_CpyTo1FPS1:				EQU 4432h
_CPYFPS1:				EQU 4435h
_CpyTo2FPS2:				EQU 4438h
_CpyTo1FPS2:				EQU 443Bh
_CPYFPS2:				EQU 443Eh
_CpyO3ToFPST:				EQU 4441h
_CpyO2ToFPST:				EQU 4444h
_CpyO6ToFPST:				EQU 4447h
_CpyO1ToFPST:				EQU 444Ah
_CpyToFPST:				EQU 444Dh
_CpyToStack:				EQU 4450h
_CpyO3ToFPS1:				EQU 4453h
_CpyO5ToFPS1:				EQU 4456h
_CpyO2ToFPS1:				EQU 4459h
_CpyO1ToFPS1:				EQU 445Ch
_CpyToFPS1:				EQU 445Fh
_CpyO2ToFPS2:				EQU 4462h
_CpyO3ToFPS2:				EQU 4465h
_CpyO6ToFPS2:				EQU 4468h
_CpyO1ToFPS2:				EQU 446Bh
_CpyToFPS2:				EQU 446Eh
_CpyO5ToFPS3:				EQU 4471h
_CpyO2ToFPS3:				EQU 4474h
_CpyO1ToFPS3:				EQU 4477h
_CpyToFPS3:				EQU 447Ah
_CpyO1ToFPS6:				EQU 447Dh
_CpyO1ToFPS7:				EQU 4480h
_CpyO1ToFPS5:				EQU 4483h
_CpyO2ToFPS4:				EQU 4486h
_CpyO1ToFPS4:				EQU 4489h
_ErrNotEnoughMem:				EQU 448Ch ;only if not HL bytes free
_FPSMINUS9:				EQU 448Fh
_HLMINUS9:				EQU 4492h
_ErrOverflow:				EQU 4495h
_ErrDivBy0:				EQU 4498h
_ErrSingularMat:				EQU 449Bh
_ErrDomain:				EQU 449Eh
_ErrIncrement:				EQU 44A1h
_ErrNon_Real:				EQU 44A4h
_ErrSyntax:				EQU 44A7h
_ErrDataType:				EQU 44AAh
_ErrArgument:				EQU 44ADh
_ErrDimMismatch:				EQU 44B0h
_ErrDimension:				EQU 44B3h
_ErrUndefined:				EQU 44B6h
_ErrMemory:				EQU 44B9h
_ErrInvalid:				EQU 44BCh
_ErrBreak:				EQU 44BFh
_ErrStat:				EQU 44C2h
_ErrSignChange:				EQU 44C5h
_ErrIterations:				EQU 44C8h
_ErrBadGuess:				EQU 44CBh
_ErrTolTooSmall:				EQU 44CEh
_ErrStatPlot:				EQU 44D1h
_ErrLinkXmit:				EQU 44D4h
_JError:				EQU 44D7h
_noErrorEntry:				EQU 44DAh
_pushErrorHandleR:				EQU 44DDh
_popErrorHandleR:				EQU 44E0h
_strcopy:				EQU 44E3h
_strCat:				EQU 44E6h
_isInSet:				EQU 44E9h
_sDone:				EQU 44ECh ;this should actually be called _SetEquToOP1
_serrort:				EQU 44EFh
_sNameEq:				EQU 44F2h
_sUnderScr:				EQU 44F5h
_sFAIL:				EQU 44F8h
_sName:				EQU 44FBh
_sOK:				EQU 44FEh
_PutMap:				EQU 4501h
_PutC:				EQU 4504h
_DispHL:				EQU 4507h
_PutS:				EQU 450Ah
_putpsb:				EQU 450Dh
_PutPS:				EQU 4510h
_wputps:				EQU 4513h
_putbuf:				EQU 4516h
_putbuf1:				EQU 4519h
_wputc:				EQU 451Ch
_wputs:				EQU 451Fh
_wputsEOL:				EQU 4522h ;displays string in HL in big font, and uses ... if too long
_wdispEOL:				EQU 4525h
_whomeup:				EQU 4528h
_setNumWindow:				EQU 452Bh ;based on current cursor position, sets winleft and similar (for input prompts)
_newline:				EQU 452Eh
_moveDown:				EQU 4531h
_scrollUp:				EQU 4534h
_shrinkWindow:				EQU 4537h
_moveUp:				EQU 453Ah
_scrollDown:				EQU 453Dh
_ClrLCDFull:				EQU 4540h
_ClrLCD:				EQU 4543h
_ClrScrnFull:				EQU 4546h
_ClrScrn:				EQU 4549h
_ClrTxtShd:				EQU 454Ch
_ClrWindow:				EQU 454Fh
_EraseEOL:				EQU 4552h
_EraseEOW:				EQU 4555h
_HomeUp:				EQU 4558h
_getcurloc:				EQU 455Bh
_VPutMap:				EQU 455Eh
_VPutS:				EQU 4561h
_VPutSN:				EQU 4564h
_vputsnG:				EQU 4567h
_vputsnT:				EQU 456Ah
_RunIndicOn:				EQU 456Dh
_RunIndicOff:				EQU 4570h
_saveCmdShadow:				EQU 4573h
_saveShadow:				EQU 4576h
_rstrShadow:				EQU 4579h
_rstrpartial:				EQU 457Ch
_rstrCurRow:				EQU 457Fh
_rstrUnderMenu:				EQU 4582h
_rstrbotrow:				EQU 4585h
_saveTR:				EQU 4588h ;save top right corner of LCD so 2nd arrow can be displayed, indicinuse flag must be set
_restoreTR:				EQU 458Bh ;restore top right corner of LCD destroyed by an arrow. indicinuse flag must be set
_GetKeyPress:				EQU 458Eh
_GetTokLen:				EQU 4591h ;input: hl=pointer to token. output: a=lenght of string, hl=pointer to string on page 1
_GET_TOK_STRNG:				EQU 4594h ;input: hl=pointer to token. output: op3=string of the token, a=length of string
_GETTOKSTRING:				EQU 4597h ;input: DE=token. output: hl=pointer to the string on page 1
_PUTBPATBUF2:				EQU 459Ah
_PUTBPATBUF:				EQU 459Dh
_putbPAT:				EQU 45A0h
_putcCheckScrolL:				EQU 45A3h
_DispEOL:				EQU 45A6h
_fdispEOL:				EQU 45A9h
_MAKEROWCMD:				EQU 45ACh
_TOTOSTRP:				EQU 45AFh
_SETVARNAME:				EQU 45B2h
_DispDone:				EQU 45B5h
_finishoutput:				EQU 45B8h
_curBlink:				EQU 45BBh
_CursorOff:				EQU 45BEh
_hideCursor:				EQU 45C1h
_CursorOn:				EQU 45C4h
_showCursor:				EQU 45C7h
_KeyToString:				EQU 45CAh
_PULLDOWNCHK:				EQU 45CDh ;something wrong here
_MenuCatCommon:				EQU 45D0h
_ZIfCatalog:				EQU 45D3h
_ZIfMatrixMenu:				EQU 45D6h ;_loadCurCat
_LoadMenuNum:				EQU 45D9h
_LoadMenuNumL:				EQU 45DCh
_MenCatRet:				EQU 45DFh ;restores display as though a menu were just cleared (restores some flags too)
_MenuSwitchContext:				EQU 45E2h ;switches to context in A, calls menu hook with A=3, set 5,(iy+16h) for some sort of override to not make switch
_MenuEdKey:				EQU 45E5h
_BackUpGraphSettings:				EQU 45E8h
_notalphnum:				EQU 45EBh
_SaveSavedFlags:				EQU 45EEh
_SetMenuFlags:				EQU 45F1h
_RstrSomeFlags:				EQU 45F4h
_RstrOScreen:				EQU 45F7h ;restores saveSScreen to the display
_SaveOScreen:				EQU 45FAh ;stores display in saveSScreen
_dispListName:				EQU 45FDh ;_SeeIfErrorCx
_PrevContext:				EQU 4600h
_CompareContext:				EQU 4603h
_AdrMRow:				EQU 4606h
_AdrMEle:				EQU 4609h
_GETMATOP1A:				EQU 460Ch
_GETM1TOOP1:				EQU 460Fh
_GETM1TOP1A:				EQU 4612h
_GetMToOP1:				EQU 4615h
_PUTTOM1A:				EQU 4618h
_PUTTOMA1:				EQU 461Bh
_PutToMat:				EQU 461Eh
_MAT_EL_DIV:				EQU 4621h
_CMATFUN:				EQU 4624h
_ROWECH_POLY:				EQU 4627h
_ROWECHELON:				EQU 462Ah
_AdrLEle:				EQU 462Dh
_GETL1TOOP1:				EQU 4630h
_GETL1TOP1A:				EQU 4633h
_GetLToOP1:				EQU 4636h
_GETL1TOOP2:				EQU 4639h
_GETL1TOP2A:				EQU 463Ch
_GETL2TOP1A:				EQU 463Fh
_PUTTOLA1:				EQU 4642h
_PutToL:				EQU 4645h
_MAXMINLST:				EQU 4648h
_LLOW:				EQU 464Bh
_LHIGH:				EQU 464Eh
_LSUM:				EQU 4651h
CUMSUM:				EQU 4654h
_ToFrac:				EQU 4657h
_SEQSET:				EQU 465Ah
_SEQSOLVE:				EQU 465Dh
_CMP_NUM_INIT:				EQU 4660h
_BinOPExec:				EQU 4663h
_EXMEAN1:				EQU 4666h
_SET2MVLPTRS:				EQU 4669h
_SETMAT1:				EQU 466Ch
_CREATETLIST:				EQU 466Fh
_UnOPExec:				EQU 4672h
_ThreeExec:				EQU 4675h
_RESTOREERRNO:				EQU 4678h
_FourExec:				EQU 467Bh
_FiveExec:				EQU 467Eh
_CPYTO2ES1:				EQU 4681h
_CPYTO6ES1:				EQU 4684h
_CPYTO1ES1:				EQU 4687h
_CPYTO3ES1:				EQU 468Ah
_CPYTO3ES2:				EQU 468Dh
_CPYTO2ES2:				EQU 4690h
_CPYTO1ES2:				EQU 4693h
_CPYTO2ES3:				EQU 4696h
_CPYTO1ES3:				EQU 4699h
_CPYTO3ES4:				EQU 469Ch
_CPYTO6ES3:				EQU 469Fh
_CPYTO2ES4:				EQU 46A2h
_CPYTO1ES4:				EQU 46A5h
_CPYTO2ES5:				EQU 46A8h
_CPYTO1ES5:				EQU 46ABh
_CPYTO4EST:				EQU 46AEh
_CPYTO2EST:				EQU 46B1h
_CPYTO1EST:				EQU 46B4h
_CPYTO2ES6:				EQU 46B7h
_CPYTO1ES6:				EQU 46BAh
_CPYTO2ES7:				EQU 46BDh
_CPYTO1ES7:				EQU 46C0h
_CPYTO2ES8:				EQU 46C3h
_CPYTO1ES8:				EQU 46C6h
_CPYTO1ES9:				EQU 46C9h
_CPYTO2ES9:				EQU 46CCh
_CPYTO2ES10:				EQU 46CFh
_CPYTO1ES10:				EQU 46D2h
_CPYTO2ES11:				EQU 46D5h
_CPYTO1ES11:				EQU 46D8h
_CPYTO2ES12:				EQU 46DBh
_CPYTO1ES12:				EQU 46DEh
_CPYTO2ES13:				EQU 46E1h
_CPYTO1ES13:				EQU 46E4h
_CPYTO1ES14:				EQU 46E7h
_CPYTO1ES16:				EQU 46EAh
_CPYTO1ES17:				EQU 46EDh
_CPYTO1ES18:				EQU 46F0h
_CPYTO1ES15:				EQU 46F3h
_CPYTO2ES15:				EQU 46F6h
_CPYO1TOEST:				EQU 46F9h
_CPYO1TOES1:				EQU 46FCh
_CPYO6TOES1:				EQU 46FFh
_CPYO6TOES3:				EQU 4702h
_CPYO1TOES2:				EQU 4705h
_CPYO2TOES2:				EQU 4708h
_CPYO1TOES3:				EQU 470Bh
_CPYO1TOES4:				EQU 470Eh
_CPYO1TOES5:				EQU 4711h
_CPYO1TOES6:				EQU 4714h
_CPYO1TOES7:				EQU 4717h
_CPYO2TOES4:				EQU 471Ah
_CPYO2TOES5:				EQU 471Dh
_CPYO2TOES6:				EQU 4720h
_CPYO2TOES7:				EQU 4723h
_CPYO2TOES8:				EQU 4726h
_CPYO2TOES9:				EQU 4729h
_CPYO1TOES8:				EQU 472Ch
_CPYO1TOES9:				EQU 472Fh
_CPYO1TOES10:				EQU 4732h
_CPYO1TOES11:				EQU 4735h
_CPYO1TOES12:				EQU 4738h
_CPYO1TOES13:				EQU 473Bh
_CPYO1TOES14:				EQU 473Eh
_CPYO1TOES15:				EQU 4741h
;_EVALF3A		equ 4744h ;Zeda: misassigned
_GetK:				EQU 4744h
_setTitle:				EQU 474Ah
_dispVarVal:				EQU 474Dh
_RecallEd:				EQU 4750h ;_setupBuffer
_createNumEditBuf:				EQU 4753h
_ProcessBufKeys:				EQU 4756h ;may be default key processing like [CLEAR], etc. especially for an edit buffer.
_CallCommon:				EQU 4759h
_CommonKeys:				EQU 475Ch
_Leftmore:				EQU 475Fh
_fDel:				EQU 4762h
_fClear:				EQU 4765h
_finsDisp:				EQU 4768h ;Michael says _FinsDisp02:				EQU 4768h (something's not right)
_FinsDisp02:				EQU 476Bh ;_setIndicator
_closeeditbufnor:				EQU 476Eh
_releaseBuffer:				EQU 4771h
_varnameToOP1hl:				EQU 4774h
_nameToOP1:				EQU 4777h
_numPPutAway:				EQU 477Ah
_numRedisp:				EQU 477Dh
_numError02:				EQU 4780h
_Load_SFont:				EQU 4783h
_SFont_Len:				EQU 4786h
_InitNumVec:				EQU 4789h ;inits window settings/table setup/finance solver context (dialog-like)
_SetXXOP1:				EQU 478Ch
_SetXXOP2:				EQU 478Fh
_SetXXXXOP2:				EQU 4792h
_UCLineS:				EQU 4795h
_CLine:				EQU 4798h
_CLineS:				EQU 479Bh
_XRootY:				EQU 479Eh
_YToX:				EQU 47A1h
_ZmStats:				EQU 47A4h
_POINT_STAT_HLP:				EQU 47A7h
_DRAWSPLOT:				EQU 47AAh
_INITNEWTRACEP:				EQU 47ADh ;A is input here, goes to (8E63h)
_SPLOTCOORD:				EQU 47B0h
_SPLOTRIGHT:				EQU 47B3h
_SPLOTLEFT:				EQU 47B6h
_CMPBOXINFO:				EQU 47B9h
_NEXTPLOT:				EQU 47BCh
_PREVPLOT:				EQU 47BFh
_CLRPREVPLOT:				EQU 47C2h
_PUT_INDEX_LST:				EQU 47C5h
_GET_INDEX_LST:				EQU 47C8h
_HEAP_SORT:				EQU 47CBh
_StoGDB2:				EQU 47CEh
_RclGDB2:				EQU 47D1h
_CircCmd:				EQU 47D4h
_GrphCirc:				EQU 47D7h
_Mov18B:				EQU 47DAh
_DarkLine:				EQU 47DDh
_ILine:				EQU 47E0h
_IPoint:				EQU 47E3h
_XYRNDBOTH:				EQU 47E6h
_XYRND:				EQU 47E9h
_CheckTOP:				EQU 47ECh
_CheckXY:				EQU 47EFh
_DarkPnt:				EQU 47F2h
_CPointS:				EQU 47F5h
_WTOV:				EQU 47F8h
_VtoWHLDE:				EQU 47FBh
_Xitof:				EQU 47FEh
_YftoI:				EQU 4801h
_XftoI:				EQU 4804h
_TraceOff:				EQU 4807h
_GrRedisp:				EQU 480Ah
_GDISPTOKEN:				EQU 480Dh
_GRDECODA:				EQU 4810h
_LABCOOR:				EQU 4813h ;draws labels with _GRLABELS and X/Y/whatever coordinates, including stat plot stuff
_COORDISP:				EQU 4816h ;draws X & Y coordinates (or R and theta if PolarGC)
_TMPEQUNOSRC:				EQU 4819h
_GRLABELS:				EQU 481Ch
_YPIXSET:				EQU 481Fh
_XPIXSET:				EQU 4822h
_COPYRNG:				EQU 4825h
_VALCUR:				EQU 4828h ;just sets/resets three flags, enables graph cursor
_GRPUTAWAY:				EQU 482Bh
_RSTGFLAGS:				EQU 482Eh
_GRReset:				EQU 4831h
_XYCENT:				EQU 4834h
_ZOOMXYCMD:				EQU 4837h
_CPTDELY:				EQU 483Ah
_CPTDELX:				EQU 483Dh
_SetFuncM:				EQU 4840h
_SetSeqM:				EQU 4843h
_SetPolM:				EQU 4846h
_SetParM:				EQU 4849h
_ZmInt:				EQU 484Ch
_ZmDecml:				EQU 484Fh
_ZmPrev:				EQU 4852h
_ZmUsr:				EQU 4855h
_SETUZM:				EQU 4858h
_ZmFit:				EQU 485Bh
_ZmSquare:				EQU 485Eh
_ZmTrig:				EQU 4861h
_SetXMinMax:				EQU 4864h
_ZooDefault:				EQU 4867h
_GrBufCpy:				EQU 486Ah
_DRAWSPLITLINE:				EQU 486Dh
_RestoreDisp:				EQU 4870h
_FNDDB:				EQU 4873h
_AllEq:				EQU 4876h
_fndallseleq:				EQU 4879h
_NEXTEQ:				EQU 487Ch
_PREVEQ:				EQU 487Fh
_BLINKGCUR:				EQU 4882h
_NBCURSOR:				EQU 4885h
_STATMARK:				EQU 4888h
_CHKTEXTCURS:				EQU 488Bh
_Regraph:				EQU 488Eh
_DOREFFLAGS02:				EQU 4891h ;something wrong here
INITNSEQ:				EQU 4894h
_YRES:				EQU 4897h ;_PLOTPTXY2
_Ceiling:				EQU 489Ah ;ceil(OP1)
_PutXY:				EQU 489Dh ;draws X & Y coordinates (regardless of PolarGC)
_PUTEQUNO:				EQU 48A0h
_PDspGrph:				EQU 48A3h
_HorizCmd:				EQU 48A6h
_VertCmd:				EQU 48A9h
_LineCmd:				EQU 48ACh
_UnLineCmd:				EQU 48AFh
_PointCmd:				EQU 48B2h
_PixelTest:				EQU 48B5h
_PixelCmd:				EQU 48B8h
_TanLnF:				EQU 48BBh
_DRAWCMD_INIT:				EQU 48BEh
_DrawCmd:				EQU 48C1h
_SHADECMD:				EQU 48C4h
_InvCmd:				EQU 48C7h
_STATSHADE:				EQU 48CAh
_dspmattable:				EQU 48CDh
_dsplsts:				EQU 48D0h
_closeEditBuf:				EQU 48D3h
_parseEditBuf:				EQU 48D6h
_putsm:				EQU 48D9h
_DspCurTbl:				EQU 48DCh
_DSPGRTBL:				EQU 48DFh
_zeroTemplate:				EQU 48E2h
_settblrefs:				EQU 48E5h
_dispTblBot:				EQU 48E8h
_DispTblTop:				EQU 48EBh
_dispTblbody:				EQU 48EEh
_VPUTBLANK:				EQU 48F1h
_TBLTRACE:				EQU 48F4h
_dispListNameY:				EQU 48F7h
_CurNameLength:				EQU 48FAh
_NameToBuf:				EQU 48FDh
_jpromptcursor:				EQU 4900h
_BufLeft:				EQU 4903h
_BufRight:				EQU 4906h
_bufInsert:				EQU 4909h
_bufQueueChar:				EQU 490Ch
_BufReplace:				EQU 490Fh
_BufDelete:				EQU 4912h
_BUFPEEK:				EQU 4915h
_BUFPEEK1:				EQU 4918h
_BUFPEEK2:				EQU 491Bh
_BUFPEEK3:				EQU 491Eh
_BufToBtm:				EQU 4921h
_setupEditEqu:				EQU 4924h
_BufToTop:				EQU 4927h
_isEditFull:				EQU 492Ah
_IsEditEmpty:				EQU 492Dh
_IsAtTop:				EQU 4930h
_IsAtBtm:				EQU 4933h
_BufClear:				EQU 4936h
_JcursorFirst:				EQU 4939h
_JcursorLast:				EQU 493Ch
_CursorLeft:				EQU 493Fh
_cursorRight:				EQU 4942h
_cursorUp:				EQU 4945h
_CursorDown:				EQU 4948h
_cursorToOffset:				EQU 494Bh
_InsDisp:				EQU 494Eh
_FDISPBOL1:				EQU 4951h
_FDISPBOL:				EQU 4954h
_DispEOW:				EQU 4957h
_DispHead:				EQU 495Ah
_DispTail:				EQU 495Dh
_PutTokString:				EQU 4960h
_setupEditCmd:				EQU 4963h
_setEmptyEditEqu:				EQU 4966h
_SetEmptyEditPtr:				EQU 4969h
_CloseEditEqu:				EQU 496Ch
_GetPrevTok:				EQU 496Fh
_getkey:				EQU 4972h
_canIndic:				EQU 4975h
_LCD_DRIVERON:				EQU 4978h 
_DFMIN2:				EQU 497Bh
_formDisp:				EQU 497Eh ;this is directly what the OS calls on the homescreen to display a result
_formMatrix:				EQU 4981h
_wscrollLeft:				EQU 4984h
_wscrollUp:				EQU 4987h
_wscrollDown:				EQU 498Ah
_wscrollRight:				EQU 498Dh
_FormEReal:				EQU 4990h
_formERealTOK:				EQU 4993h
_FormDCplx:				EQU 4996h
_FormReal:				EQU 4999h
_formScrollUp:				EQU 499Ch
_setwinabove:				EQU 499Fh
_disarmScroll:				EQU 49A2h
_OP1toEdit:				EQU 49A5h
_MinToEdit:				EQU 49A8h
_rclVarToEdit:				EQU 49ABh
_rclVarToEditPtR:				EQU 49AEh
_RCLENTRYTOEDIT:				EQU 49B1h
_rclToQueue:				EQU 49B4h ;recalls bytes at OP1 into edit buffer
_FORMTOTOK:				EQU 49B7h
_DISP_INTERVAL:				EQU 49BAh
_DisplstName:				EQU 49BDh
_dispSLstNameHL:				EQU 49C0h
_EditEqu:				EQU 49C3h
_closeEquField:				EQU 49C6h
_AutoSelect:				EQU 49C9h
_DISPYEOS:				EQU 49CCh
_dispNumEOS:				EQU 49CFh
_setupdispeq:				EQU 49D2h
_DispForward:				EQU 49D5h
_DispYPrompt2:				EQU 49D8h
_stringwidth:				EQU 49DBh
_dispErrorScreen:				EQU 49DEh ;displays top row of error screen (error message)
_POPCX:				EQU 49E1h ;moves 14 bytes at cxPrev to cxMain, 15th byte goes to replace appflags
_loadnoeentry:				EQU 49E4h
_SaveScreen:				EQU 49E7h
_RETSCREEN:				EQU 49EAh
_RetScreenErr:				EQU 49EDh
_CheckSplitFlag:				EQU 49F0h
_SolveRedisp:				EQU 49F3h
_SolveDisp:				EQU 49F6h
_itemName:				EQU 49F9h
_SetNorm_Vals:				EQU 49FCh
_SetYOffset:				EQU 49FFh ;sets up YOffset and next 4 bytes (possibly specialized for the table editor)
_ConvKeyToTok:				EQU 4A02h
_ConvFCKeyToTok:				EQU 4A05h
_ConvFEKeyToTok:				EQU 4A08h
_TokToKey:				EQU 4A0Bh
_SendSkipExitPacket:				EQU 4A0Eh
_GETVARCMD:				EQU 4A11h
_SendVarCmd:				EQU 4A14h
_SendScreenshot:				EQU 4A17h
keyscnlnk:				EQU 4A1Ah
_DeselectAllVars:				EQU 4A1Dh
_DelRes:				EQU 4A20h
_ConvLcToLr:				EQU 4A23h
_RedimMat:				EQU 4A26h
_IncLstSize:				EQU 4A29h
_InsertList:				EQU 4A2Ch
_dellistel:				EQU 4A2Fh
_EditProg:				EQU 4A32h
_CloseProg:				EQU 4A35h
_ClrGraphRef:				EQU 4A38h
_FixTempCnt:				EQU 4A3Bh
_SAVEDATA:				EQU 4A3Eh
_RESTOREDATA:				EQU 4A41h
_FindAlphaUp:				EQU 4A44h
_FindAlphaDn:				EQU 4A47h
_CmpSyms:				EQU 4A4Ah
_CREATETEMP:				EQU 4A4Dh
_CleanAll:				EQU 4A50h
_MoveToNextSym:				EQU 4A53h ;input: hl=pointer to type byte of VAT entry. output: hl = pointer to next entry's type byte
_ConvLrToLc:				EQU 4A56h
_TblScreenDn:				EQU 4A59h ;something is not right here
_TblScreenUp:				EQU 4A5Ch
_SCREENUP:				EQU 4A5Fh
_ScreenUpDown:				EQU 4A62h
_ZifRclHandler:				EQU 4A65h
_zifrclkapp:				EQU 4A68h
_rclkeyRtn:				EQU 4A6Bh
_RCLKEY:				EQU 4A6Bh
_RCLREGEQ_CALL:				EQU 4A6Eh
_RCLREGEQ:				EQU 4A71h
_initNamePrompt:				EQU 4A74h
_NamePrompt2:				EQU 4A77h
_CATALOGCHK:				EQU 4A7Ah
_clrTR:				EQU 4A7Dh
_QUAD:				EQU 4A80h
_GRAPHQUAD:				EQU 4A83h
_BC2NOREAL:				EQU 4A86h
_ErrNonReal_FPST_FPS1:				EQU 4A89h
_ErrNonReal:				EQU 4A8Ch ;ERR:DATA TYPE if top B numers from FPS are non-real
_WRITE_TEXT:				EQU 4A8Fh
_FORSEQINIT:				EQU 4A92h
_GRPHPARS:				EQU 4A95h
_PLOTPARS:				EQU 4A98h
_ParseInp:				EQU 4A9Bh
_PARSEOFF:				EQU 4A9Eh
_PARSESCAN:				EQU 4AA1h
_GETPARSE:				EQU 4AA4h
_SAVEPARSE:				EQU 4AA7h
_InitPFlgs:				EQU 4AAAh
_CKENDLINERR:				EQU 4AADh
_OP2Set60:				EQU 4AB0h
_GETSTATPTR:				EQU 4AB3h
_CMP_STATPTR:				EQU 4AB6h
_VARSYSADR:				EQU 4AB9h
_StoSysTok:				EQU 4ABCh
_StoAns:				EQU 4ABFh
_StoTheta:				EQU 4AC2h
_StoR:				EQU 4AC5h
_StoY:				EQU 4AC8h
_StoN:				EQU 4ACBh
_StoT:				EQU 4ACEh
_StoX:				EQU 4AD1h
_StoOther:				EQU 4AD4h
_RclAns:				EQU 4AD7h
_RclY:				EQU 4ADAh
_RclN:				EQU 4ADDh
_RclX:				EQU 4AE0h
_RclVarSym:				EQU 4AE3h
_RclSysTok:				EQU 4AE6h
_StMatEl:				EQU 4AE9h
_STLSTVECEL:				EQU 4AECh
_ConvOP1:				EQU 4AEFh
_Find_Parse_Formula:				EQU 4AF2h
_PARSE_FORMULA:				EQU 4AF5h
_FetchQuotedString:				EQU 4AF8h
_FetchNumLine:				EQU 4AFBh
_ParseNameTokens:				EQU 4AFEh
_ParseInpGraph:				EQU 4B01h ;same as _ParseInp except 3,(iy+1Fh) is graph/split screen override, or something
_ParseInpGraphReset:				EQU 4B04h ;_ParseInpGraph except zeroes out iy+6/7, resets 3,(iy+1Ah) & 0,(iy+1Fh), fmtFlags->fmtOverride, parse within ParseInp?
_ParseInpLastEnt:				EQU 4B07h ;ParseInp on program 05h,23h,00h
_ErrOnCertainTypes:				EQU 4B0Ah ;ERR:DATA TYPE if A is one of a couple of values...subroutine in ParseInp, somehow
_CreatePair:				EQU 4B0Dh
_PUSHNUM:				EQU 4B10h
_INCCURPCERREND:				EQU 4B13h
_ERREND:				EQU 4B16h
_COMMAERRF:				EQU 4B19h
_COMMAERR:				EQU 4B1Ch
_STEQARG2:				EQU 4B1Fh
_STEQARG:				EQU 4B22h
_INPARG:				EQU 4B25h
_STEQARG3:				EQU 4B28h
_NXTFETCH:				EQU 4B2Bh
_CKFETCHVAR:				EQU 4B2Eh
_FETCHVARA:				EQU 4B31h
_FETCHVAR:				EQU 4B34h
_CKENDLIN:				EQU 4B37h ;gets parse byte in A and then _CKENDEXP
_CKENDEXP:				EQU 4B3Ah ;checks A for 3Eh or 3Fh
_CKPARSEND:				EQU 4B3Dh
_STOTYPEARG:				EQU 4B40h
_ConvDim:				EQU 4B43h
_ConvDim00:				EQU 4B46h
_AHEADEQUAL:				EQU 4B49h
_PARSAHEADS:				EQU 4B4Ch
_PARSAHEAD:				EQU 4B4Fh
_AnsName:				EQU 4B52h
_STOCMPREALS:				EQU 4B55h
_GETDEPTR:				EQU 4B58h
_PUSH2BOPER:				EQU 4B5Bh ;push the value in bc onto the operator stack
_POP2BOPER:				EQU 4B5Eh ;pop 2 bytes on the operator stack to bc
_PUSHOPER:				EQU 4B61h ;push the value in a onto the operator stack
_POPOPER:				EQU 4B64h ;pop 1 byte on the operator stack to a
_FIND_E_UNDEF:				EQU 4B67h
_STTMPEQ:				EQU 4B6Ah
_FINDEOL:				EQU 4B6Dh
_BRKINC:				EQU 4B70h
_INCFETCH:				EQU 4B73h
_CURFETCH:				EQU 4B76h
_Random:				EQU 4B79h
_StoRand:				EQU 4B7Ch
_RandInit:				EQU 4B7Fh
_resetStacks:				EQU 4B82h ;(onsp)->(errsp), (fpbase)->(fps), (opbase)->(ops)
_Factorial:				EQU 4B85h
_YONOFF:				EQU 4B88h
_EQSELUNSEL:				EQU 4B8Bh
_ITSOLVER:				EQU 4B8Eh
_GRITSOLVER:				EQU 4B91h
_ITSOLVERB:				EQU 4B94h
_ITSOLVERNB:				EQU 4B97h
_ExTest_INT:				EQU 4B9Ah
_DIST_FUN:				EQU 4BADh
_LogGamma:				EQU 4BA0h
_OneVar:				EQU 4BA3h
_ONEVARS_0:				EQU 4BA6h
_ORDSTAT:				EQU 4BA9h
_INITSTATANS2:				EQU 4BACh
_ANOVA_SPEC:				EQU 4BAFh
_OutputExpr:				EQU 4BB2h
_CentCursor:				EQU 4BB5h
_TEXT:				EQU 4BB8h
_FINISHSPEC:				EQU 4BBBh
_TRCYFUNC:				EQU 4BBEh
_RCL_SEQ_X:				EQU 4BC1h
_RCLSEQ2:				EQU 4BC4h
_GRPPutAway:				EQU 4BC7h
_CKVALDELX:				EQU 4BCAh
_CKVALDELTA:				EQU 4BCDh
_GrBufClr:				EQU 4BD0h
_GRBUFCPY_V:				EQU 4BD3h
_FNDSELEQ:				EQU 4BD6h
_CLRGRAPHXY:				EQU 4BD9h
_NEDXT_Y_STYLE:				EQU 4BDCh
_PLOTPT:				EQU 4BDFh
_NEWINDEP:				EQU 4BE2h
_Axes:				EQU 4BE5h
_setPenX:				EQU 4BE8h
_setPenY:				EQU 4BEBh
_setPenT:				EQU 4BEEh
_TAN_EQU_DISP:				EQU 4BF1h
_PutAns:				EQU 4BF4h
_DispOP1A:				EQU 4BF7h
_MATHTANLN:				EQU 4BFAh
_ENDDRAW:				EQU 4BFDh
_SetTblGraphDraw:				EQU 4C00h
_StartDialog:				EQU 4C03h
_DialogInit:				EQU 4C06h
_GetDialogNumOP1:				EQU 4C09h
_SetDialogNumOP1:				EQU 4C0Ch
_GetDialogNumHL:				EQU 4C0Fh
_ErrArgumentO123:				EQU 4C12h ;ERR:ARGUMENT if OP2>OP1 or OP1>OP3
_SetDialogKeyOverride:				EQU 4C15h
_ResDialogKeyOverride:				EQU 4C18h
_ForceDialogKeypress:				EQU 4C1Bh
_DialogStartGetKey:				EQU 4C1Eh
_StartDialog_Override:				EQU 4C21h
_CallDialogCallback:				EQU 4C24h
_SetDialogCallback:				EQU 4C27h
_ResDialogCallback:				EQU 4C2Ah
_CopyDialogNum:				EQU 4C2Dh
_MemClear:				EQU 4C30h
_MemSet:				EQU 4C33h
_ReloadAppEntryVecs:				EQU 4C36h
_PointOn:				EQU 4C39h
_ExecuteNewPrgm:				EQU 4C3Ch
_StrLength:				EQU 4C3Fh
_VPutMapRec:				EQU 4C42h
_getRomPage:				EQU 4C45h
_FindAppUp:				EQU 4C48h
_FindAppDn:				EQU 4C4Bh
_FindApp:				EQU 4C4Eh
_ExecuteApp:				EQU 4C51h
_MonReset:				EQU 4C54h
_ClearParseVar:				EQU 4C57h
_SetParseVarProg:				EQU 4C5Ah
_isContextKey:				EQU 4C5Dh
_IBounds:				EQU 4C60h
_IOffset:				EQU 4C63h
_DrawCirc2:				EQU 4C66h
_CanAlphIns:				EQU 4C69h
cxRedisp:				EQU 4C6Ch
_GetBaseVer:				EQU 4C6Fh
_OPSet0DE:				EQU 4C72h ;loads a floating point 0 to location de 
_AppGetCbl:				EQU 4C75h
_AppGetCalc:				EQU 4C78h
_SaveDisp:				EQU 4C7Bh
_SetIgnoreKey:				EQU 4C7Eh ;set 1,(iy+28h) / ret
_SetSendThisKeyBack:				EQU 4C81h ;set 2,(iy+28h) / ld (kbdKey),a / ret
_DisableApd:				EQU 4C84h
_EnableApd:				EQU 4C87h ;set apdable,(iy+apdflags)
_JForceCmdNoChar2:				EQU 4C8Ah ;2.41 at least
_set2IY34:				EQU 4C8Dh ;set 2,(iy+34) / ret
_forcecmd:				EQU 4C90h
_ApdSetup:				EQU 4C93h
_Get_NumKey:				EQU 4C96h
_AppSetup:				EQU 4C99h ;or _AppCleanup, or something
_HandleLinkKeyActivity:				EQU 4C9Ch
_JForceCmdNoChar3:				EQU 4C9Fh ;2.41 at least
_ReleaseSedit:				EQU 4CA2h
_initsmalleditline:				EQU 4CA5h
_startsmalledit:				EQU 4CA8h
;4CABh
_SGetTokString:				EQU 4CAEh
_LoadPattern:				EQU 4CB1h
_SStringLength:				EQU 4CB4h
_RestorePenCol:				EQU 4CB7h
;4CBAh
_DoNothing:				EQU 4CBDh
_ForceSmallEditReturn:				EQU 4CC0h
;4CC3h ;saves context
;4CC6h
;4CC9h
;4CCCh
_VEraseEOL:				EQU 4CCFh
;4CD2h
;4CD5h
_GoToErr:				EQU 4CD8h
_initsmalleditBox:				EQU 4CDBh
;4CDEh
_EmptyHook:				EQU 4CE1h
_ForceSmallEditReturn2:				EQU 4CE4h
;4CE7h ;same as 4CC3h
;4CEAh
_ClearRow:				EQU 4CEDh
;4CF0h
;4CF3h
;4CF6h
;4CF9h
;4CFCh
;4CFFh
;4D02h
;4D05h
;4D08h
;4D0Bh
;4D0Eh
;4D11h
;4D14h
;4D17h
;4D1Ah
;4D1Dh
;4D20h
;4D23h
_AppScreenUpDown:				EQU 4D26h ;shifts screen up/down, A is LCD row, H is number of lines to shift, (OP1)-(OP1+3) are something
_AppScreenUpDown1:				EQU 4D29h ;shifts screen up/down, but really no clue what the inputs are (all registers and (OP1)-(OP1+3))
;4D2Ch
_initsmalleditlinevar:				EQU 4D2Fh
_initsmalleditlineop1:				EQU 4D32h
_initsmalleditboxvar:				EQU 4D35h
_initsmalleditboxop1:				EQU 4D38h
;4D3Bh
_RestartDialog:				EQU 4D3Eh
_ErrCustom1:				EQU 4D41h
_ErrCustom2:				EQU 4D44h
_AppStartMouse:				EQU 4D47h
_AppStartMouseNoSetup:				EQU 4D4Ah
_AppMouseGetKey:				EQU 4D4Dh
_AppDispMouse:				EQU 4D50h
_AppEraseMouse:				EQU 4D53h
_AppSetupMouseMem:				EQU 4D56h
_GetDispRowOffset:				EQU 4D59h ;HL=A*12 (intended for A to be row and HL becomes offset into plotSScreen)
_ClearRect:				EQU 4D5Ch
_InvertRect:				EQU 4D5Fh
_FillRect:				EQU 4D62h
_AppUpdateMouse:				EQU 4D65h
_AppDispPrevMouse:				EQU 4D68h ;might bring previous keypress's movement to current coordinates with flags to not display
;4D6Bh ;restores some cursor flags and stuff
_initcellbox:				EQU 4D6Eh
_drawcell:				EQU 4D71h
;4D74h
_invertcell:				EQU 4D77h
_setcelloverride:				EQU 4D7Ah
_DrawRectBorder:				EQU 4D7Dh
_ClearCell:				EQU 4D80h
_covercell:				EQU 4D83h
_EraseRectBorder:				EQU 4D86h
_FillRectPattern:				EQU 4D89h
_DrawRectBorderClear:				EQU 4D8Ch
;4D8Fh ;mouse subroutine
;4D92h
_VerticalLine:				EQU 4D95h
_IBoundsFull:				EQU 4D98h
_DisplayImage:				EQU 4D9Bh
;4D9Eh ;does something dumb with ports 10h/11h
;4DA1h ;mouse subroutine
_AppUpdateMouseCoords:				EQU 4DA4h
_ShiftBitsLeft:				EQU 4DA7h ;mouse subroutine, shifts B bits left from DE sprite to HL one
;4DAAh ;mouse subroutine
;4DADh ;mouse subroutine
;4DB0h ;mouse subroutine
;4DB3h ;mouse subroutine
;4DB6h ;mouse subroutine
;4DB9h ;mouse subroutine
;4DBCh ;mouse subroutine
_AppUpdateMouseRow:				EQU 4DBFh
_AppDrawMouse:				EQU 4DC2h ;set 2,(iy+2Ch) for AppEraseMouse, reset for AppDispMouse
_AppDrawMouseDirect:				EQU 4DC5h ;pretty much _AppDrawMouse, but you pass LCD column in A
_CPoint:				EQU 4DC8h
_DeleteApp:				EQU 4DCBh
_AppUpdateMouseXY:				EQU 4DCEh
_setmodecellflag:				EQU 4DD1h
_resetmodecellflag:				EQU 4DD4h
_ismodecellset:				EQU 4DD7h
_getmodecellflag:				EQU 4DDAh
;4DDDh
_CellBoxManager:				EQU 4DE0h
_startnewcell:				EQU 4DE3h
;4DE6h
_CellCursorHandle:				EQU 4DE9h
;4DECh
;4DEFh
_ClearCurCell:				EQU 4DF2h
_drawcurcell:				EQU 4DF5h
_invertcurcell:				EQU 4DF8h
_covercurcell:				EQU 4DFBh
_BlinkCell:				EQU 4DFEh
_BlinkCellNoLookUp:				EQU 4E01h
_BlinkCurCell:				EQU 4E04h
_BlinkCellToOn:				EQU 4E07h
_BlinkCellToOnNoLookUp:				EQU 4E0Ah
_BlinkCurCellToOn:				EQU 4E0Dh
_BlinkCellToOff:				EQU 4E10h
_BlinkCellToOffNoLookUp:				EQU 4E13h
_BlinkCurCellToOff:				EQU 4E16h
_getcurmodecellflag:				EQU 4E19h
;4E1Ch
_startsmalleditreturn:				EQU 4E1Fh
;4E22h
;4E25h
_CellkHandle:				EQU 4E28h
_errchkalphabox:				EQU 4E2Bh
;4E2Eh
;4E31h
;4E34h
;4E37h
_eraseallcells:				EQU 4E3Ah
_iscurmodecellset:				EQU 4E3Dh
;4E40h
_initalphabox:				EQU 4E43h
;4E46h
;4E49h
_drawblnkcell:				EQU 4E4Ch
_ClearBlnkCell:				EQU 4E4Fh
_invertblnkcell:				EQU 4E52h
_AppMouseForceKey:				EQU 4E55h
_AppSetupMouseMemCoords:				EQU 4E58h ;this is _AppSetupMouseMem except you pass starting coordinates in HL
_AppMoveMouse:				EQU 4E5Bh ;this is _AppMouseForceKey and then updating coordinates
_GetStringInput:				EQU 4E5Eh
_GetStringInput2:				EQU 4E61h
_WaitEnterKeyValue:				EQU 4E64h
_HorizontalLine:				EQU 4E67h
_CreateAppVar:				EQU 4E6Ah
_CreateProtProg:				EQU 4E6Dh
_CreateVar:				EQU 4E70h
_AsmComp:				EQU 4E73h
_GetAsmSize:				EQU 4E76h
_SquishPrgm:				EQU 4E79h
_ExecutePrgm:				EQU 4E7Ch
_ChkFindSymAsm:				EQU 4E7Fh
_ParsePrgmName:				EQU 4E82h
_CSub:				EQU 4E85h
_CAdd:				EQU 4E88h
_CSqaure:				EQU 4E8Bh
_CMult:				EQU 4E8Eh
_CRecip:				EQU 4E91h
_CDiv:				EQU 4E94h
_CAbs:				EQU 4E97h
_AddSquares:				EQU 4E9Ah
_CSqRoot:				EQU 4E9Dh
_CLN:				EQU 4EA0h
_CLog:				EQU 4EA3h
_CTenX:				EQU 4EA6h
_CEtoX:				EQU 4EA9h
_CXrootY:				EQU 4EACh
;4EAFh
_CYtoX:				EQU 4EB2h
_InvertNonReal:				EQU 4EB5h
_CplxMult:				EQU 4EB8h
_CplxDiv:				EQU 4EBBh
_CplxTrunc:				EQU 4EBEh
_CplxFrac:				EQU 4EC1h
_CplxFloor:				EQU 4EC4h
_SendHeaderPacket:				EQU 4EC7h
_CancelTransmission:				EQU 4ECAh
_SendScreenContents:				EQU 4ECDh
_SendRAMVarData:				EQU 4ED0h
_SendRAMCmd:				EQU 4ED3h
_SendPacket:				EQU 4ED6h
_ReceiveAck:				EQU 4ED9h
_Send4BytePacket:				EQU 4EDCh
_SendDataByte:				EQU 4EDFh
_Send4Bytes:				EQU 4EE2h
_SendAByte:				EQU 4EE5h
_SendCByte:				EQU 4EE8h
_GetSmallPacket:				EQU 4EEBh
_GetDataPacket:				EQU 4EEEh
_SendAck:				EQU 4EF1h
_Get4Bytes:				EQU 4EF4h
_Get3Bytes:				EQU 4EF7h
_Rec1stByte:				EQU 4EFAh
_Rec1stByteNC:				EQU 4EFDh
_ContinueGetByte:				EQU 4F00h
_RecAByteIO:				EQU 4F03h
_ReceiveVar:				EQU 4F06h
_ReceiveVarDataExists:				EQU 4F09h
_ReceiveVarData:				EQU 4F0Ch
_SrchVLstUp:				EQU 4F0Fh
_SrchVLstDn:				EQU 4F12h
_SendVariable:				EQU 4F15h
_Get4BytesCursor:				EQU 4F18h
_Get4BytesNC:				EQU 4F1Bh
_Convert85List:				EQU 4F1Eh
_SendDirectoryContents:				EQU 4F21h
_SendReadyPacket:				EQU 4F24h
_Convert85Real:				EQU 4F27h
_ret_6:				EQU 4F2Ah
_SendCertificate:				EQU 4F2Dh ;sends certificate in header/data packets, Flash must be unlocked, used with sending an application in LINK menu
_SendApplication:				EQU 4F30h
_SendOSHeader:				EQU 4F33h
_SendOSPage:				EQU 4F36h
_SendOS:				EQU 4F39h
_FlashWriteDisable:				EQU 4F3Ch
_SendCmd:				EQU 4F3Fh
_SendOSValidationData:				EQU 4F42h
_Disp:				EQU 4F45h
_SendGetkeyPress:				EQU 4F48h
_RejectCommand:				EQU 4F4Bh
_CheckLinkLines:				EQU 4F4Eh
_GetHookByte:				EQU 4F51h
_GetBytePaged:				EQU 4F54h
_cursorhook:				EQU 4F57h
_call_library_hook:				EQU 4F5Ah
_call_rawkey_hook:				EQU 4F5Dh
_setCursorHook:				EQU 4F60h ;enable cursor hook
_EnableLibraryHook:				EQU 4F63h
_SetGetKeyHook:				EQU 4F66h
_ClrCursorHook:				EQU 4F69h
_DisableLibraryHook:				EQU 4F6Ch
_ClrRawKeyHook:				EQU 4F6Fh
_ResetHookBytes:				EQU 4F72h
_AdjustAllHooks:				EQU 4F75h
_getkeyhook:				EQU 4F78h
_SetGetcscHook:				EQU 4F7Bh
_ClrGetKeyHook:				EQU 4F7Eh
_call_linkactivity_hook:				EQU 4F81h
_EnableLinkActivityHook:				EQU 4F84h
_DisableLinkHook:				EQU 4F87h
_GetSmallPacket2:				EQU 4F8Ah
_EnableCatalog2Hook:				EQU 4F8Dh
_DisableCatalog2Hook:				EQU 4F90h
_EnableLocalizeHook:				EQU 4F93h
_DisableLocalizeHook:				EQU 4F96h
_SetTokenHook:				EQU 4F99h
_ClearTokenHook:				EQU 4F9Ch
;4F9Fh ld hl,92c6 / ld a,(92c5) / res 2,a / cp (hl) / ret
;4FA2h hl=11*(92fc)+92c9 / ld a,(hl) / and Fh / cp 2 / ret ; I can almost guarantee this is stat plot related
_DispListElementOffLA:				EQU 4FA5h
_Bit_VertSplit:				EQU 4FA8h
_SetHomescreenHook:				EQU 4FABh
_ClrHomeScreenHook:				EQU 4FAEh
_SetWindowHook:				EQU 4FB1h
_DisableWindowHook:				EQU 4FB4h
_SetGraphModeHook:				EQU 4FB7h
_DisableGraphHook:				EQU 4FBAh
_ParseAndStoreSysVar:				EQU 4FBDh
_DisplayEditSysVar:				EQU 4FC0h
_JForceWindowSettings:				EQU 4FC3h
_DelVarArc:				EQU 4FC6h
_DelVarNoArc:				EQU 4FC9h
_SetAllPlots:				EQU 4FCCh
_SetYeditHook:				EQU 4FCFh
_DisableYEquHook:				EQU 4FD2h
_JForceYEqu:				EQU 4FD5h
_Arc_Unarc:				EQU 4FD8h ;checks for low battery
_ArchiveVar:				EQU 4FDBh ;set 0,(iy+24h) to check for low battery first
_UnarchiveVar:				EQU 4FDEh
_DialogKeyHook:				EQU 4FE1h ;rawkey hook used by OS for dialog context
_SetFontHook:				EQU 4FE4h
_ClrFontHook:				EQU 4FE7h
_SetRegraphHook:				EQU 4FEAh
_DisableRegraphHook:				EQU 4FEDh
_RunGraphingHook:				EQU 4FF0h
_SetTraceHook:				EQU 4FF3h
_DisableTraceHook:				EQU 4FF6h
_RunTraceHook:				EQU 4FF9h
_NDeriv:				EQU 4FFCh
_PolarDerivative:				EQU 4FFFh
_JForceGraphNoKey:				EQU 5002h
_JForceGraphKey:				EQU 5005h
_PowerOff:				EQU 5008h
_GetKeyRetOff:				EQU 500Bh ;same as getkey, only returns kOff if 2nd+on is pressed 
_FindGroupSym:				EQU 500Eh
_FillBasePageTable:				EQU 5011h
_ArcChk:				EQU 5014h
_FlashToRam:				EQU 5017h
_LoadDEIndPaged:				EQU 501Ah
_LoadCIndPaged:				EQU 501Dh
_SetupPagedPtr:				EQU 5020h
_PagedGet:				EQU 5023h
_SetParserHook:				EQU 5026h
_ClearParserHook:				EQU 5029h
_SetAppChangeHook:				EQU 502Ch
_ClearAppChangeHook:				EQU 502Fh
_EnableGraphicsHook:				EQU 5032h
_DisableGraphicsHook:				EQU 5035h
_IPointNoGraphicsHook:				EQU 5038h
_ILineNoHook:				EQU 503Bh
;503Eh
_DeleteTempPrograms:				EQU 5041h
_EnableCatalog1Hook:				EQU 5044h
_DisableCatalog1Hook:				EQU 5047h
_EnableHelpHook:				EQU 504Ah
_DisableHelpHook:				EQU 504Dh
_DispCatalogEnd:				EQU 5050h
_GetMenuKeypress:				EQU 5053h
_GetCatalogItem:				EQU 5056h
_RunCatalog2Hook:				EQU 5059h
_RunCatalog1Hook:				EQU 505Ch
;505Fh
;5062h
_dispMenuTitle:				EQU 5065h
;5068h
_EnablecxRedispHook:				EQU 506Bh
_DisablecxRedispHook:				EQU 506Eh
_BufCpy:				EQU 5071h
_BufClr:				EQU 5074h
_UnOPExec2:				EQU 5077h
_BinOPExec2:				EQU 507Ah
_LoadMenuB:				EQU 507Dh ;clears screen and loads menu from B, plus a couple flag changes
_DisplayVarInfo:				EQU 5080h
_SetMenuHook:				EQU 5083h
_ClearMenuHook:				EQU 5086h
_getBCOffsetIX:				EQU 5089h
_GetBCOffsetIX2:				EQU 508Ch
_ForceFullScreen:				EQU 508Fh
_GetVariableData:				EQU 5092h
_FindSwapSector:				EQU 5095h
_CopyFlashPage:				EQU 5098h
_FindAppNumPages:				EQU 509Bh
_HLMinus5:				EQU 509Eh
_SendArcPacket:				EQU 50A1h
_ForceGraphKeypress:				EQU 50A4h
_DoNothing3:				EQU 50A7h
_FormBase:				EQU 50AAh
;50ADh
_IsFragmented:				EQU 50B0h
_Chk_Batt_Low:				EQU 50B3h
_Chk_Batt_Low_2:				EQU 50B6h
_Arc_Unarc2:				EQU 50B9h ;identical to _Arc_Unarc, except you can choose to res 0,(iy+24h) to skip low battery check
_GetAppBasePage:				EQU 50BCh ;input: a=one of an app's pages. output: a=app's first page
_SetExSpeed:				EQU 50BFh
_RclExit:				EQU 50C2h
_GroupAllVars:				EQU 50C5h
_UngroupVar:				EQU 50C8h
_WriteToFlash:				EQU 50CBh ;ReceiveApplication or something like that on OSes below 2.40
_SetSilentLinkHook:				EQU 50CEh
_DisableSilentLinkHook:				EQU 50D1h
_TwoVarSet:				EQU 50D4h
_ExecClassCToken:				EQU 50D7h
_ExecClass3Token:				EQU 50DAh
_GetSysInfo:				EQU 50DDh
_NZIf83Plus:				EQU 50E0h
_LinkStatus:				EQU 50E3h
_DoNothing2:				EQU 50E6h ;originally for TI-Navigator
_KeyboardGetKey:				EQU 50E9h
_RunAppLib:				EQU 50ECh
_FindSpecialAppHeader:				EQU 50EFh
_SendUSBData:				EQU 50F2h
_AppGetCBLUSB:				EQU 50F5h
_AppGetCalcUSB:				EQU 50F8h
_GetVarCmdUSB:				EQU 50FBh
;50FEh
_TenX2:				EQU 5101h
;5104h
;5107h
_GetVarVersion:				EQU 510Ah
;510Dh
;5110h
_DeleteTempEditEqu:				EQU 5113h
_JcursorFirst2:				EQU 5116h
;5119h
_PromptMoveBackLeft:				EQU 511Ch
_wputsEOL2:				EQU 511Fh ;same except res 0,(iy+0Eh) first
_InvertTextInsMode:				EQU 5122h
;5125h
_ResetDefaults:				EQU 5128h
_ZeroFinanceVars:				EQU 512Bh
_DispHeader:				EQU 512Eh
_JForceGroup:				EQU 5131h
;5134h
;5137h
_DispCoords:				EQU 513Ah
;513Dh
;5140h
_chkTmr:				EQU 5143h
;5146h
;5149h
;514Ch
_getDate:				EQU 514Fh
_GetDateString:				EQU 5152h
_getDtFmt:				EQU 5155h
_getDtStr:				EQU 5158h
_getTime:				EQU 515Bh
_GetTimeString:				EQU 515Eh
_getTmFmt:				EQU 5161h
_getTmStr:				EQU 5164h
_SetZeroOne:				EQU 5167h
_setDate:				EQU 516Ah
_IsOneTwoThree:				EQU 516Dh
_setTime:				EQU 5170h
_IsOP112or24:				EQU 5173h
_chkTimer0:				EQU 5176h
_timeCnv:				EQU 5179h
_GetLToOP1Extra:				EQU 517Ch
_ClrWindowAndFlags:				EQU 517Fh
_SetMachineID:				EQU 5182h
_ResetLists:				EQU 5185h
_DispValue:				EQU 5188h
;518Bh
;518Eh
_ExecLib:				EQU 5191h
;5194h
_CPOP1OP2Rounded:				EQU 5197h
_CPOP1OP2Rounded2:				EQU 519Ah
_OpenLib:				EQU 519Dh
;51A0h
;51A3h
_ResetIOPrompt:				EQU 51A6h
_StrCpyVarData:				EQU 51A9h
_SetUpEditor:				EQU 51ACh
_SortA:				EQU 51AFh
_SortD:				EQU 51B2h
;51B5h
_IsOP1ResID:				EQU 51B8h
_ListEdNameCxMain:				EQU 51BBh
_ListEdEnterNewName:				EQU 51BEh
;51C1h
_ForceModeKeypress:				EQU 51C4h ;forces a keypress (and calls help hook) on any of several mode-setting contexts
_DispAboutScreen:				EQU 51C7h
_ChkHelpHookVer:				EQU 51CAh
_Disp32:				EQU 51CDh
;51D0h
;51D3h
;51D6h
;51D9h
_DrawTableEditor:				EQU 51DCh ;draws table editor lines
_DisplayListNameEquals:				EQU 51DFh
_DisplayListHeader:				EQU 51E2h
_DispMatrixDimensions:				EQU 51E5h
_HighlightListEdItem:				EQU 51E8h
;51EBh
;51EEh
_MatrixName:				EQU 51F1h
;51F4h
;51F7h
;51FAh
;51FDh
;5200h
;5203h
;5206h
;5209h
;520Ch
;520Fh
_SetupEmptyEditTempEqu:				EQU 5212h
_ExecClass1Token:				EQU 5215h
_HandleMathTokenParse:				EQU 5218h
_MaybePushMultiplyOp:				EQU 521Bh
_RestartParseOP1Result:				EQU 521Eh
_Chk_Batt_Level:				EQU 5221h
;5224h
;5227h
;522Ah
_DisplayListEquals:				EQU 522Dh
_GetCurPlotListOffset:				EQU 5230h
_GoToLastRow:				EQU 5233h
_RectBorder:				EQU 5236h
;5239h
;523Ch
;523Fh
_LoadA5:				EQU 5242h
;5245h
_NamedListToOP1:				EQU 5248h
;524Bh
;524Eh
;5251h
_InitUSBDeviceCallback:				EQU 5254h
_KillUSBDevice:				EQU 5257h ;this actually recycles the USB connection and re-inits it (I think)
_SetUSBConfiguration:				EQU 525Ah
_RequestUSBData:				EQU 525Dh
_StopReceivingUSBData:				EQU 5260h
_FindAppHeaderByPage:				EQU 5263h
_FindNextHeaderByPage:				EQU 5266h
_IsMatchingLaunchApp:				EQU 5269h
_InitTimer:				EQU 526Ch
_KillTimer:				EQU 526Fh
_StartTimer:				EQU 5272h
_RestartTimer:				EQU 5275h
_StopTimer:				EQU 5278h
_WaitTimer:				EQU 527Bh
_CheckTimer:				EQU 527Eh
_CheckTimerRestart:				EQU 5281h
_SetVertGraphActive:				EQU 5284h
_ClearVertGraphActive:				EQU 5287h
_EnableUSBHook:				EQU 528Ah
_DisableUSBHook:				EQU 528Dh
_InitUSBDevice:				EQU 5290h
_KillUSBPeripheral:				EQU 5293h
_GetCurPlotListOffset2:				EQU 5296h
;5299h
_GraphLine:				EQU 529Ch
;529Fh
;52A2h
;52A5h
;52A8h
;52ABh
;52AEh
_ZifTableEditor:				EQU 52B1h
;52B4h
_GetCurPlotOffset:				EQU 52B7h
;52BAh
_FindAppName:				EQU 52BDh
;52C0h
;52C3h
_UpdateStatPlotLists:				EQU 52C6h
_GrBufCpyCustom:				EQU 52C9h
;52CCh
;52CFh
;52D2h
_VDispRealOP1:				EQU 52D5h
_DispXEqualsNum:				EQU 52D8h
_ResetGraphSettings:				EQU 52DBh
_InitializeVariables:				EQU 52DEh
;52E1h ;bit 4,(9C75h) (this is DEFINITELY returning the status of something when acting as a TI-SmartView Input Pad...this bit is bit 1 of the data byte from a PC HID Set Report request)
_DelVarSym:				EQU 52E4h
_FindAppUpNoCase:				EQU 52E7h
_FindAppDnNoCase:				EQU 52EAh
_DeleteInvalidApps:				EQU 52EDh
_DeleteApp_Link:				EQU 52F0h
_CmpSymsNoCase:				EQU 52F3h
_SetAppRestrictions:				EQU 52F6h
_RemoveAppRestrictions:				EQU 52F9h
_QueryAppRestrictions:				EQU 52FCh
_DispAppRestrictions:				EQU 52FFh
_SetupHome:				EQU 5302h
_GRPUTAWAYFull:				EQU 5305h ;same as _GRPUTAWAY except it assumes no split screen
_SendSmartPadKeypress:				EQU 5308h ;B and A are the inputs
_ToggleUSBSmartPadInput:				EQU 530Bh ;A is input, 0 or 1 to enable/disable
_IsUSBDeviceConnected:				EQU 530Eh ;bit 4,(81h) | ret, this is just a guess on its purpose but it seems to work
_RecycleUSB:				EQU 5311h ;identical to 5257h
_PolarEquToOP1:				EQU 5314h
_ParamXEquToOP1:				EQU 5317h
_ParamYEquToOP1:				EQU 531Ah
_GetRestrictionsOptions:				EQU 531Dh
_DispResetComplete:				EQU 5320h
_PTTReset:				EQU 5323h
_FindAppCustom:				EQU 5326h
_ClearGraphStyles:				EQU 5329h
;532Ch
;532Fh
;5332h
;5335h
;5338h
;533Bh
;533Eh
;5341h
;5344h
;5347h
;534Ah
;534Dh
;5350h
;5353h
;5356h
;5359h
;535Ch
;535Fh
;5362h
;5365h
;5368h
;536Bh
;536Eh
;5371h
;5374h
;5377h
;537Ah
;537Dh
;5380h
;5383h
;5386h
;5389h
;538Ch
;538Fh
;5392h
;5395h
;5398h
;539Bh
;539Eh
;53A1h
;53A4h
;53A7h
;53AAh
;53ADh
;53B0h
;53B3h
;53B6h
;53B9h
;53BCh
;53BFh
;53C2h
;53C5h
;53C8h
;53CBh
;53CEh
;53D1h
;53D4h
;53D7h
;53DAh
;53DDh
;53E0h
;53E3h
;53E6h
;53E9h
;53ECh
;53EFh
;53F2h
;53F5h
;53F8h
;53FBh
;53FEh
;5401h
;5404h
;5407h
;540Ah
;540Dh
;5410h
;5413h
;5416h
;5419h
;541Ch
;541Fh
;5422h
;5425h
;5428h
;542Bh
;542Eh
;5431h
;5434h
;5437h
;543Ah
;543Dh
;5440h
;5443h
;5446h
;5449h
_xorAret:				EQU 5443h
_scfRet:				EQU 5446h
_ret:				EQU 5449h

;Page 1Fh ROM Calls
;--------------------------------
bootbtf:				EQU 8000h
;400Fh may point to version string ("1.02 ",0)
_MD5Final:				EQU 8018h
_RSAValidate:				EQU 801Bh
_cmpStr:				EQU 801Eh ;BigNumCompare
_WriteAByte:				EQU 8021h
_EraseFlash:				EQU 8024h
_FindFirstCertField:				EQU 8027h
_ZeroToCertificate:				EQU 802Ah
_GetCertificateEnd:				EQU 802Dh
_FindGroupedField:				EQU 8030h
_ret_1:				EQU 8033h
_ret_2:				EQU 8036h
_ret_3:				EQU 8039h
_ret_4:				EQU 803Ch
_ret_5:				EQU 803Fh
_Mult8By8:				EQU 8042h
_Mult16By8:				EQU 8045h
_Div16By8:				EQU 8048h
_Div16By16:				EQU 804Bh
;804Eh ;scary certificate reading and writing, something about calc ID and fields 0A10/0A20
_LoadAIndPaged:				EQU 8051h
_FlashToRam2:				EQU 8054h
_GetCertificateStart:				EQU 8057h
_GetFieldSize:				EQU 805Ah
_FindSubField:				EQU 805Dh
_EraseCertificateSector:				EQU 8060h
_CheckHeaderKey:				EQU 8063h
;8066h ;just returns Z if specified data in field 0310h, subfield 0610h exists, DE points to data of that field you want to find
;8069h ;just returns number of 0810h/0710h fields that exist in certificate or something, in IX
_Load_LFontV2:				EQU 806Ch
_Load_LFontV:				EQU 806Fh
_ReceiveOS:				EQU 8072h
_FindOSHeaderSubField:				EQU 8075h
_FindNextCertField:				EQU 8078h
_GetByteOrBoot:				EQU 807Bh
_getSerial:				EQU 807Eh ;GetCalcSerial
_ReceiveCalcID:				EQU 8081h ;receives certificate replacement (including calculator ID, fails if already exists) and writes it, requires Flash unlocked
_EraseFlashPage:				EQU 8084h
_WriteFlashUnsafe:				EQU 8087h
_dispBootVer:				EQU 808Ah
_MD5Init:				EQU 808Dh
_MD5Update:				EQU 8090h
_MarkOSInvalid:				EQU 8093h
_FindProgramLicense:				EQU 8096h ;copies 8010h field to appID and other insane stuff that makes zero sense
_MarkOSValid:				EQU 8099h
_CheckOSValidated:				EQU 809Ch
_SetupAppPubKey:				EQU 809Fh
_SigModR:				EQU 80A2h
_TransformHash:				EQU 80A5h
_IsAppFreeware:				EQU 80A8h
_FindAppHeaderSubField:				EQU 80ABh
_WriteValidationNumber:				EQU 80AEh ;generates two-byte validation number from calc ID and stores to certificate
_Div32By16:				EQU 80B1h
_FindGroup:				EQU 80B4h ;searches until field of DE-like group is found (DE=0A00h, it stops when it finds 0Ax0h)
_getBootVer:				EQU 80B7h
_getHardwareVersion:				EQU 80BAh
_xorA:				EQU 80BDh ;xor a
_bignumpowermod17:				EQU 80C0h
_ProdNrPart1:				EQU 80C3h
_WriteAByteSafe:				EQU 80C6h
_WriteFlash:				EQU 80C9h
_SetupDateStampPubKey:				EQU 80CCh
_SetFlashLowerBound:				EQU 80CFh
_LowBatteryBoot:				EQU 80D2h
;TI-84 Plus/Silver Edition Only Entry Points
_AttemptUSBOSReceive:				EQU 80E4h ;Z to wait for USB cable insert & get OS, NZ and A= contents of port 4Dh or 56h, ON to cancel & clear RAM
_DisplayBootMessage:				EQU 80E7h
_NewLine2:				EQU 80EAh
_DisplayBootError10:				EQU 80EDh
_Chk_Batt_Low_B:				EQU 80F0h
_Chk_Batt_Low_B2:				EQU 80F3h
_ReceiveOS_USB:				EQU 80F6h
_DisplayOSProgress:				EQU 80F9h
_ResetCalc:				EQU 80FCh
_SetupOSPubKey:				EQU 80FFh
_CheckHeaderKeyHL:				EQU 8102h ;same as _CheckHeaderKey, only you pass the address of header in HL, not at appData
_USBErrorCleanup:				EQU 8105h ;kills some USB stuff (doesn't completely kill periph communication), error handler in boot code
_InitUSB:				EQU 8108h ;initializes USB hardware as peripheral, sets 5,(iy+1Bh), C set if problems
;810Bh set 1,(81h) and wait (has something to do with USB peripheral kill, but it doesn't actually kill it)
_KillUSB:				EQU 810Eh ;identical to 8105h, except in the middle of the outputs, it sends zero to port 4Ch
_DisplayBootError1:				EQU 8111h
_DisplayBootError2:				EQU 8114h
_DisplayBootError3:				EQU 8117h
_DisplayBootError4:				EQU 811Ah
_DisplayBootError5:				EQU 811Dh
_DisplayBootError6:				EQU 8120h
_DisplayBootError7:				EQU 8123h
_DisplayBootError8:				EQU 8126h
_DisplayBootError9:				EQU 8129h

;RAM Equates
;--------------------------------
ramStart:				EQU 8000h
appData:				EQU 8000h
ramCode:				EQU 8100h
SmallEditColumnLeft:				EQU 8177h
SmallEditRow:				EQU 8178h
SmallEditColumnRight:				EQU 8179h
;penCol left edge?	equ 817Bh
bigInteger1:				EQU 8182h
SmallEditCancelParse:				EQU 8194h
SmallEditRowCount:				EQU 81B7h
bigInteger2:				EQU 81C3h
SmallEditPromptString:				EQU 81CCh
ramCodeEnd:				EQU 822Fh
baseAppBrTab:				EQU 8230h ;table of base pages for apps on page < 20h (starts with eight zeroes because they're OS pages)
clockFlag:				EQU 8230h ;bit 2 set for 24-hour mode and don't display "AM/PM"
clockIDs:				EQU 8231h ;five bytes, numbers 0-4 in memory, that when present, stops displaying clock numbers in time setting context?
bootTemp:				EQU 8251h
MD5Temp:				EQU 8259h
MD5Length:				EQU 8269h
MD5Hash:				EQU 8292h
appSearchPage:				EQU 82A3h
tempSwapArea:				EQU 82A5h
;something		equ 837Bh ;18 bytes, probably indicates something about Flash app pages, start out as 0FFh
appID:				EQU 838Dh
arcPageEnd:				EQU 8392h
arcPtrEnd:				EQU 8393h
;839Fh something...field size bytes?
MD5Buffer:				EQU 83A5h
Abackup:				EQU 83EBh
ramReturnData:				EQU 83EDh
arcInfo:				EQU 83EEh
savedArcInfo:				EQU 8406h
appInfo:				EQU 8432h
appBank_jump:				EQU 843Ch
appPage:				EQU 843Eh
kbdScanCode:				EQU 843Fh
kbdKey:				EQU 8444h
kbdGetKy:				EQU 8445h
keyExtend:				EQU 8446h
EXTECHO:				EQU keyExtend
contrast:				EQU 8447h
apdSubTimer:				EQU 8448h
apdTimer:				EQU 8449h
curTime:				EQU 844Ah
curRow:				EQU 844Bh
curCol:				EQU 844Ch
curOffset:				EQU 844Dh
curUnder:				EQU 844Eh
curY:				EQU 844Fh
curType:				EQU 8450h
curXRow:				EQU 8451h
prevDData:				EQU 8452h
lFont_record:				EQU 845Ah
sFont_record:				EQU 8462h
tokVarPtr:				EQU 846Ah
tokLen:				EQU 846Ch
indicMem:				EQU 846Eh ;eight bytes used by _saveTR and _restoreTR to store image in top right corner
indicCounter:				EQU 8476h
indicBusy:				EQU 8477h
OP1:				EQU 8478h
OP1M:				EQU 847Ah
OP2:				EQU 8483h
OP2M:				EQU 8485h
OP2EXT:				EQU 848Ch
OP3:				EQU 848Eh
OP3M:				EQU 8490h
OP4:				EQU 8499h
OP4M:				EQU 849Bh
OP5:				EQU 84A4h
OP5M:				EQU 84A6h
OP6:				EQU 84AFh
OP6M:				EQU 84B1h
OP6EXT:				EQU 84B8h
progToEdit:				EQU 84BFh
nameBuff:				EQU 84C7h
equ_edit_save:				EQU 84D2h
iMathPtr1:				EQU 84D3h
iMathPtr2:				EQU 84D5h
iMathPtr3:				EQU 84D7h
iMathPtr4:				EQU 84D9h
iMathPtr5:				EQU 84DBh
chkDelPtr1:				EQU 84DDh
chkDelPtr2:				EQU 84DFh
insDelPtr:				EQU 84E1h
upDownPtr:				EQU 84E3h
fOutDat:				EQU 84E5h
asm_data_ptr1:				EQU 84EBh
asm_data_ptr2:				EQU 84EDh
asm_sym_ptr1:				EQU 84EFh
asm_sym_ptr2:				EQU 84F1h
asm_ram:				EQU 84F3h
asm_ind_call:				EQU 8507h
textShadow:				EQU 8508h
textShadCur:				EQU 8588h
textShadTop:				EQU 858Ah
textShadAlph:				EQU 858Bh
textShadIns:				EQU 858Ch
cxMain:				EQU 858Dh
cxPPutAway:				EQU 858Fh
cxPutAway:				EQU 8591h
cxErrorEP:				EQU 8595h
cxSizeWind:				EQU 8597h
cxPage:				EQU 8599h
cxCurApp:				EQU 859Ah
cxPrev:				EQU 859Bh ;12 bytes are shadows of cxMain through cxCurApp and appFlags
monQH:				EQU 85AAh
monQT:				EQU 85ABh
monQueue:				EQU 85ACh
onSP:				EQU 85BCh
promptRow:				EQU 85C0h
promptCol:				EQU 85C1h
promptIns:				EQU 85C2h
promptShift:				EQU 85C3h
promptRet:				EQU 85C4h
promptValid:				EQU 85C6h
promptTop:				EQU 85C8h
promptCursor:				EQU 85CAh
promptTail:				EQU 85CCh
promptBtm:				EQU 85CEh
varType:				EQU 85D0h
varCurrent:				EQU 85D1h
varClass:				EQU 85D9h
CatalogCurrent:				EQU 85DAh ;word at this location starting with 6007h corresponds to what is highlighted in catalog
menuActive:				EQU 85DCh
menuAppDepth:				EQU 85DDh
MenuCurrent:				EQU 85DEh
;			equ 85DFh ;holds current submenu index
;			equ 85E0h ;holds currently selected item in current submenu
;			equ 85E1h ;holds number of submenus for this menu
;			equ 85E2h ;holds number of items in this submenu
;			equ 85E3h ;iy+appFlags backup for menu stuff
;			equ 85E4h ;iy+0Ch backup for menu stuff
;			equ 85E5h ;curGStyle backup for menu stuff
;			equ 85E6h ;iy+graphFlags backup for menu stuff
ProgCurrent:				EQU 85E8h
;something, OP1 backup?	equ 85F2h ;type and name of topmost variable on menu with names
;something		equ 85FDh
userMenuSA:				EQU 85FEh
ioPrompt:				EQU 865Fh
dImageWidth:				EQU 8660h
ioFlag:				EQU 8670h
sndRecState:				EQU 8672h
ioErrState:				EQU 8673h
header:				EQU 8674h
ioData:				EQU 867Dh
ioNewData:				EQU 8689h
bakHeader:				EQU 868Bh
;something		equ 8697h ;app bitmap for selecting stuff from menus
;something		equ 86B7h ;used in 47h and 74h link packets
penCol:				EQU 86D7h
penRow:				EQU 86D8h
rclQueue:				EQU 86D9h
rclQueueEnd:				EQU 86DBh
errNo:				EQU 86DDh
errSP:				EQU 86DEh
errOffset:				EQU 86E0h
saveSScreen:				EQU 86ECh
asm_prgm_size:				EQU 89ECh
bstCounter:				EQU 89EEh
flags:				EQU 89F0h
appFlagsAddr:				EQU 89FDh
;something		equ 8A36h ;stats-related? This gets stored to (varCurrent) for some reason
statVars:				EQU 8A3Ah
anovaf_vars:				EQU 8C17h
infVars:				EQU 8C4Dh
infVar1:				EQU 8C56h
infVar2:				EQU 8C5Fh
infVar3:				EQU 8C68h
infVar4:				EQU 8C71h
infVar5:				EQU 8C7Ah
infVar6:				EQU 8C83h
infVar7:				EQU 8C8Ch
infVar8:				EQU 8C95h
infVar9:				EQU 8C9Eh
infVar10:				EQU 8CA7h
infVar11:				EQU 8CB0h
infVar12:				EQU 8CB9h
infVar13:				EQU 8CC2h
infVar14:				EQU 8CCBh
infVar15:				EQU 8CD4h
infVar16:				EQU 8CDDh
infVar17:				EQU 8CE6h
infVar18:				EQU 8CEFh
infVar19:				EQU 8CF8h
infVar20:				EQU 8D01h
;something		equ 8D0Bh
;list-related stat vars	equ 8D0Dh
curGStyle:				EQU 8D17h
curGY:				EQU 8D18h
curGX:				EQU 8D19h
curGY2:				EQU 8D1Ah
curGX2:				EQU 8D1Bh ;currently selected:				EQUation while graphing
freeSaveY:				EQU 8D1Ch
freeSaveX:				EQU 8D1Dh
;100 bytes		equ 8D2Ah
XOffset:				EQU 8DA1h
YOffset:				EQU 8DA2h
lcdTallP:				EQU 8DA3h
pixWideP:				EQU 8DA4h
pixWide_m_1:				EQU 8DA5h
pixWide_m_2:				EQU 8DA6h
lastEntryPTR:				EQU 8DA7h ;pointer to the next available byte in the entry stack
lastEntryStk:				EQU 8DA9h ;the start of entry stack (note last entry is not in the stack, it is in the program '#'.)  This is a stack of strings.  first 2 bytes are length, followed by string. 2nd from last entry is first in this stack.
numLastEntries:				EQU 8E29h ;number of entries you can back-track through minus one
currLastEntry:				EQU 8E2Ah ;counter used by OS to keep track of which entry was just displayed by pressing 2nd+enter
curPlotNumber:				EQU 8E63h ;current plot being graphed (1-3), this gets reset back to 0
;something		equ 8E65h
;something		equ 8E66h
curInc:				EQU 8E67h
uXmin:				EQU 8E7Eh
uXmax:				EQU 8E87h
uXscl:				EQU 8E90h
uYmin:				EQU 8E99h
uYmax:				EQU 8EA2h
uYscl:				EQU 8EABh
uThetMin:				EQU 8EB4h
uThetMax:				EQU 8EBDh
uThetStep:				EQU 8EC6h
uTmin:				EQU 8ECFh
uTmax:				EQU 8ED8h
uTStep:				EQU 8EE1h
uPlotStart:				EQU 8EEAh
unMax:				EQU 8EF3h
uu0:				EQU 8EFCh
uv0:				EQU 8F05h
unMin:				EQU 8F0Eh
uu02:				EQU 8F17h
uv02:				EQU 8F20h
uw0:				EQU 8F29h
uPlotStep:				EQU 8F32h
uXres:				EQU 8F3Bh
uw02:				EQU 8F44h
Xmin:				EQU 8F50h
Xmax:				EQU 8F59h
Xscl:				EQU 8F62h
Ymin:				EQU 8F6Bh
Ymax:				EQU 8F74h
Yscl:				EQU 8F7Dh
ThetaMin:				EQU 8F86h
ThetaMax:				EQU 8F8Fh
ThetaStep:				EQU 8F98h
TminPar:				EQU 8FA1h
TmaxPar:				EQU 8FAAh
Tstep:				EQU 8FB3h
PlotStart:				EQU 8FBCh
nMax:				EQU 8FC5h
u0:				EQU 8FCEh
v0:				EQU 8FD7h
nMin:				EQU 8FE0h
u02:				EQU 8FE9h
v02:				EQU 8FF2h
w0:				EQU 8FFBh
PlotStep:				EQU 9004h
XresO:				EQU 900Dh
w02:				EQU 9016h
un1:				EQU 901Fh
un2:				EQU 9028h
vn1:				EQU 9031h
vn2:				EQU 903Ah
wn1:				EQU 9043h
wn2:				EQU 904Ch
fin_N:				EQU 9055h
fin_I:				EQU 905Eh
fin_PV:				EQU 9067h
fin_PMT:				EQU 9070h
fin_FV:				EQU 9079h
fin_PY:				EQU 9082h
fin_CY:				EQU 908Bh
cal_N:				EQU 9094h
cal_I:				EQU 909Dh
cal_PV:				EQU 90A6h
cal_PMT:				EQU 90AFh
cal_FV:				EQU 90B8h
cal_PY:				EQU 90C1h
smallEditRAM:				EQU 90D3h
XFact:				EQU 913Fh
YFact:				EQU 9148h
Xres_int:				EQU 9151h
deltaX:				EQU 9152h
deltaY:				EQU 915Bh
shortX:				EQU 9164h
shortY:				EQU 916Dh
lower:				EQU 9176h
upper:				EQU 917Fh
XOutSym:				EQU 918Ch
XOutDat:				EQU 918Eh
YOutSym:				EQU 9190h
YOutDat:				EQU 9192h
inputSym:				EQU 9194h
inputDat:				EQU 9196h
prevData:				EQU 9198h
;something		equ 91D9h
;something		equ 91DAh
CurTableRow:				EQU 91DCh
CurTableCol:				EQU 91DDh
TblMin:				EQU 92B3h
TblStep:				EQU 92BCh
;something		equ 92C5h
;something		equ 92C6h
;somePlotThing1		equ 92D9h
;somePlotThing2		equ 92EAh
;somePlotThing3		equ 92FBh
ES:				EQU 9302h ;bottom of the es
EST:				EQU 9305h ;current height of the es
;something		equ 9311h ;this is the pointer to a table of stuff for a BASIC menu
plotSScreen:				EQU 9340h
seed1:				EQU 9640h
seed2:				EQU 9649h
basic_prog:				EQU 9652h
basic_start:				EQU 965Bh
nextParseByte:				EQU 965Dh ;basic_pc
basic_end:				EQU 965Fh
numArguments:				EQU 9661h
;something		equ 9665h ;parser-related word
;something		equ 966Ch
;something		equ 966Dh
cmdShadow:				EQU 966Eh
cmdShadCur:				EQU 96EEh
cmdShadAlph:				EQU 96F0h
cmdShadIns:				EQU 96F1h
cmdCursor:				EQU 96F2h
editTop:				EQU 96F4h
editCursor:				EQU 96F6h
editTail:				EQU 96F8h
editBtm:				EQU 96FAh
;something		equ 96FEh ;word, this is offset into list for currently-highlighted element in list editor
;something		equ 9700h ;table entry pointer used in dialog/menu/edit buffer routines
matrixDimensions:				EQU 9702h ;dimensions of matrix being edited in matrix editor
editSym:				EQU 9706h ;pointer to symbol table entry of variable being edited
editDat:				EQU 9708h ;pointer to data of variable being edited
;something		equ 970Eh ;stats/list editor related, usually 1
;something		equ 970Fh ;stats/list editor related, usually 0 (this is 0-based offset from listName1), pretty sure this is a page offset
;something		equ 9710h ;stats/list editor related, usually 0 (this is 0-based currently-selected list)
listName1:				EQU 9711h
listName2:				EQU 9716h
listName3:				EQU 971Bh
listName4:				EQU 9720h
listName5:				EQU 9725h
listName6:				EQU 972Ah
listName7:				EQU 972Fh
listName8:				EQU 9734h
listName9:				EQU 9739h
listName10:				EQU 973Eh
listName11:				EQU 9743h
listName12:				EQU 9748h
listName13:				EQU 974Dh
listName14:				EQU 9752h
listName15:				EQU 9757h
listName16:				EQU 975Ch
listName17:				EQU 9761h
listName18:				EQU 9766h
listName19:				EQU 976Bh
listName20:				EQU 9770h
;something		equ 9775h
y1LineType:				EQU 9776h ;these bytes define the line type for functions which are graphed
y2LineType:				EQU 9777h
y3LineType:				EQU 9778h
y4LineType:				EQU 9779h
y5LineType:				EQU 977Ah
y6LineType:				EQU 977Bh
y7LineType:				EQU 977Ch
y8LineType:				EQU 977Dh
y9LineType:				EQU 977Eh
y0LineType:				EQU 977Fh
para1LineType:				EQU 9780h
para2LineType:				EQU 9781h
para3LineType:				EQU 9782h
para4LineType:				EQU 9783h
para5LineType:				EQU 9784h
para6LineType:				EQU 9785h
polar1LineType:				EQU 9786h
polar2LineType:				EQU 9787h
polar3LineType:				EQU 9788h
polar4LineType:				EQU 9789h
polar5LineType:				EQU 978Ah
polar6LineType:				EQU 978Bh
secULineType:				EQU 978Ch
secVLineType:				EQU 978Dh
secWLineType:				EQU 978Eh
;something		equ 979Fh
;something		equ 97A1h
winTop:				EQU 97A5h
winBtm:				EQU 97A6h
winLeftEdge:				EQU 97A7h
winLeft:				EQU 97A8h
winAbove:				EQU 97AAh
winRow:				EQU 97ACh
winCol:				EQU 97AEh
fmtDigits:				EQU 97B0h
fmtString:				EQU 97B1h
fmtConv:				EQU 97F2h
fmtLeft:				EQU 9804h
fmtIndex:				EQU 9806h
fmtMatSym:				EQU 9808h
fmtMatMem:				EQU 980Ah
EQS:				EQU 980Ch
;something		equ 980Eh
;something		equ 9810h
freeRAM:				EQU 9815h ;pretty sure this is the amount of RAM free, valid in Mem Mgmt/Del anyway
;something		equ 9817h
tSymPtr1:				EQU 9818h
tSymPtr2:				EQU 981Ah
chkDelPtr3:				EQU 981Ch
chkDelPtr4:				EQU 981Eh
tempMem:				EQU 9820h
fpBase:				EQU 9822h
FPS:				EQU 9824h
OPBase:				EQU 9826h
OPS:				EQU 9828h
pTempCnt:				EQU 982Ah
cleanTmp:				EQU 982Ch
pTemp:				EQU 982Eh ;end of symbol table
progPtr:				EQU 9830h
newDataPtr:				EQU 9832h
pagedCount:				EQU 9834h
pagedPN:				EQU 9835h
pagedGetPtr:				EQU 9836h
pagedPutPtr:				EQU 9838h
pagedBuf:				EQU 983Ah
;something		equ 984Ah ;this is the top LCD row for a menu (usually 1)
appErr1:				EQU 984Dh
appErr2:				EQU 985Ah
flashByte1:				EQU 9867h
flashByte2:				EQU 9868h
freeArcBlock:				EQU 9869h
arcPage:				EQU 986Bh
arcPtr:				EQU 986Ch
appRawKeyHandle:				EQU 9870h
appBackUpScreen:				EQU 9872h
customHeight:				EQU 9B72h
localLanguage:				EQU 9B73h
hookExecTemp:				EQU 9B75h
linkActivityHook:				EQU 9B78h
cursorHookPtr:				EQU 9B7Ch
libraryHookPtr:				EQU 9B80h
rawKeyHookPtr:				EQU 9B84h
getKeyHookPtr:				EQU 9B88h
homescreenHookPtr:				EQU 9B8Ch
windowHookPtr:				EQU 9B90h
graphHookPtr:				EQU 9B94h
yEqualsHookPtr:				EQU 9B98h
fontHookPtr:				EQU 9B9Ch
regraphHookPtr:				EQU 9BA0h
graphicsHook:				EQU 9BA4h
traceHookPtr:				EQU 9BA8h
parserHookPtr:				EQU 9BACh
appChangeHookPtr:				EQU 9BB0h
catalog1HookPtr:				EQU 9BB4h
helpHookPtr:				EQU 9BB8h
cxRedispHookPtr:				EQU 9BBCh
menuHookPtr:				EQU 9BC0h
catalog2HookPtr:				EQU 9BC4h
tokenHookPtr:				EQU 9BC8h
localizeHookPtr:				EQU 9BCCh
silentLinkHookPtr:				EQU 9BD0h ;restartClr?
USBActivityHookPtr:				EQU 9BD4h
baseAppBrTab2:				EQU 9C06h ;table of base pages for apps on page >= 20h; first 20h bytes are zeroes.
USBcallbackPage:				EQU 9C13h
USBcallbackPtr:				EQU 9C14h
vendorID:				EQU 9C16h
productID:				EQU 9C18h
devReleaseNumber:				EQU 9C1Ah
oldSESpeed:				EQU 9C21h
calcPeripheralState:				EQU 9C26h
usb_dataWaitingCount:				EQU 9C27h
basePageTable2End:				EQU 9C6Fh ;this is the last page
port91hTemp:				EQU 9C79h
usb_dataExpectCount:				EQU 9C80h
offPageCallPage:				EQU 9C83h ;temporarily stores off-page page and address
offPageCallAddress:				EQU 9C84h
;something		equ 9C86h ;used with SE link activity, values of 00h, 0FAh, 0FFh probably mean something
;something		equ 9C87h ;zero this to force the APPS menu to recache itself (this is always either 0, 1, or last app page, for some reason)
;something		equ 9C88h ;don't know, but it's 14 bytes and has to do with selecting stuff (if 9C87h is non-zero, this is zeroed out)
;something		equ 9C9Eh ;funky, this actually gets written to the certificate on non-83+ (11 bytes)
SEspeed:				EQU 9CAEh
;something, table index	equ 9CAFh
;something		equ 9CB0h
;something		equ 9CB1h ;table
;something		equ 9CCDh
localTokStr:				EQU 9D65h
keyForStr:				EQU 9D76h
keyToStrRam:				EQU 9D77h
sedMonSp:				EQU 9D88h ;small edit monitor SP
bpSave:				EQU 9D8Ah
userMem:				EQU 9D95h
symTable:				EQU 0FE66h

;System Flags
;----------------------------------------------------------------------
ioDelFlag:				EQU 0h
inDelete:				EQU 0		;1 = DELETE SCREEN 

trigFlags:				EQU 0h	;Trigonometry mode settings
trigDeg:				EQU 2		;1=degrees, 0=radians

kbdFlags:				EQU 0h	;Keyboard scan
kbdSCR:				EQU 3		;1=scan code ready
kbdKeyPress:				EQU 4		;1=key has been pressed

doneFlags:				EQU 0h	;display "Done"
donePrgm:				EQU 5		;1=display "Done" after prgm
;----------------------------------------------------------------------
editFlags:				EQU 1h
editOpen:				EQU 2		;1=edit buffer is open

ansFlags:				EQU 1
AnsScroll:				EQU 3		;1=answer can scroll, seems must be reset in order to move about edit buffer

monFlags:				EQU 1h	 ;monitor flags
monAbandon:				EQU 4		;1=don't start any long process in put away (#715)
;----------------------------------------------------------------------
plotFlags:				EQU 2h	;plot generation flags
plotLoc:				EQU 1		;0=bkup & display, 1=display only
plotDisp:				EQU 2		;1=plot is in display, 0=text in display, this also indicates whether graph is being shown or not

grfModeFlags:				EQU 2h	;graph mode settings
grfFuncM:				EQU 4		;1=function graph
grfPolarM:				EQU 5		;1=polar graph
grfParamM:				EQU 6		;1=parametric graph
grfRecurM:				EQU 7		;1=RECURSION graph
;----------------------------------------------------------------------
graphFlags:				EQU 3h
graphDraw:				EQU 0		;0=graph is valid, 1=redraw graph(dirty)
graphCursor:				EQU 2
;----------------------------------------------------------------------
grfDBFlags:				EQU 4h
grfDot:				EQU 0		;0=line, 1=dot
grfSimul:				EQU 1		;0=sequential, 1=simultaneous
grfGrid:				EQU 2		;0=no grid, 1=grid
grfPolar:				EQU 3		;0=rectangular, 1=polar coordinates
grfNoCoord:				EQU 4		;0=display coordinates, 1=off
grfNoAxis:				EQU 5		;0=axis, 1=no axis
grfLabel:				EQU 6		;0=off, 1=axis label
;----------------------------------------------------------------------
textFlags:				EQU 5h	;Text output flags
textEraseBelow:				EQU 1		;1=erase line below small char
textScrolled:				EQU 2		;1=screen scrolled
textInverse:				EQU 3		;1=display inverse bit-map
textInsMode:				EQU 4		;0=overstrike, 1=insert mode
;----------------------------------------------------------------------
ParsFlag:				EQU 6h	;PARSER flags
;----------------------------------------------------------------------
ParsFlag2:				EQU 7h	;PARSER flags
numOP1:				EQU 0		;1=RESULT IN OP1, 0=NO RESULT
;----------------------------------------------------------------------
newDispF:				EQU 8h	;Derivative mode flags
preClrForMode:				EQU 0		;1=HELP BLINK ON MODE SCREEN
allowProgTokens:				EQU 1		;1=allow programming tokens to be parsed in BASIC programs

apdFlags:				EQU 8h	;Automatic power-down
apdAble:				EQU 2		;1=APD enabled
apdRunning:				EQU 3		;1=APD clock running
apdWarmStart:				EQU 4		;1=calculator is turning on from APD or power loss
;----------------------------------------------------------------------
web_err_mask:				EQU 60h
;----------------------------------------------------------------------
onFlags:				EQU 9h	;on key flags
parseInput:				EQU 1		;1=parse input when done
onRunning:				EQU 3		;1=calculator is running
onInterrupt:				EQU 4		;1=on key interrupt request

statFlags:				EQU 9h	;statistics flags
;unknown		equ 5		;unknown
statsValid:				EQU 6		;1=stats are valid
;----------------------------------------------------------------------
fmtFlags:				EQU 0Ah	;numeric format flags
fmtExponent:				EQU 0	 	;1=show exponent, 0=no exponent
fmtEng:				EQU 1	 	;1=engineering notion, 0=scientific
fmtHex:				EQU 2	 	;1=hexadecimal
fmtOct:				EQU 3	 	;1=octal
fmtBin:				EQU 4	 	;1=binary

numMode:				EQU 0Ah
fmtReal:				EQU 5
fmtRect:				EQU 6
fmtPolar:				EQU 7

realMode:				EQU 5
rectMode:				EQU 6
polarMode:				EQU 7
;					;if Hex and Oct both = 1
;					; then Bin=0 means >Frac
;					; Bin=1 means >DMS
fmtBaseMask:				EQU  00011100b	; mask to base flags
fmtBaseShift:				EQU  2		; offset to base flags
;
;       CHECK IF THESE ARE USED BY NUMFORM,
;
;               	equ  6
;               	equ  7
;----------------------------------------------------------------------
fmtOverride:				EQU 0Bh	;copy of fmtFlags with conversion override
;----------------------------------------------------------------------
fmtEditFlags:				EQU 0Ch	;numeric editing flags
fmtEdit:				EQU 0		;1=format number for editing

curFlags:				EQU 0Ch	;Cursor
curAble:				EQU 2		;1=cursor flash is enabled
curOn:				EQU 3		;1=cursor is showing
curLock:				EQU 4		;1=cursor is locked off

cmdFlags:				EQU 0Ch	;command editor flags
cmdVirgin:				EQU 5		;1=nothing has been typed in cmd bfr
;----------------------------------------------------------------------
appFlags:				EQU 0Dh	;application flags
appWantIntrpt:				EQU 0		;1=want ON key interrupts
appTextSave:				EQU 1		;1=save characters in textShadow
appAutoScroll:				EQU 2		;1=auto-scroll text on last line
appMenus:				EQU 3		;1=process keys that bring up menus, 0=check Lock menu flag
appLockMenus:				EQU 4		;1=ignore menu keys, 0=switch to home screen and bring up menu
appCurGraphic:				EQU 5		;1=graphic cursor
appCurWord:				EQU 6		;1=text cursor covers entire word
appExit:				EQU 7		;1=application handles [EXIT] key itself

appWantIntrptF:				EQU 1<<appWantIntrpt
appTextSaveF:				EQU 1<<appTextSave
appAutoScrollF:				EQU 1<<appAutoScroll
appMenusF:				EQU 1<<appMenus
appLockMenusF:				EQU 1<<appLockMenus
appCurGraphicF:				EQU 1<<appCurGraphic
appCurWordF:				EQU 1<<appCurWord
appExitF:				EQU 1<<appExit
;----------------------------------------------------------------------
rclFlag:				EQU 0Eh	;OS recall queue flags
enableQueue:				EQU 7		;1 = enable recall queue
;----------------------------------------------------------------------
seqFlags:				EQU 0Fh	;Sequential Graph flags
webMode:				EQU 0		;0 = NORMAL SEQ MODE, 1 = WEB MODE
webVert:				EQU 1
sequv:				EQU 2		;U vs V
seqvw:				EQU 3		;V vs W
sequw:				EQU 4		;U vs W
;----------------------------------------------------------------------
promptFlags:				EQU 11h	;prompt line flags
promptEdit:				EQU 0		;1=editing in prompt buffer
;----------------------------------------------------------------------
indicFlags:				EQU 12h	;Indicator flags
indicRun:				EQU 0		;1=run indicator ON
indicInUse:				EQU 1		;indicator save area in use=1, free=0 ;resetting will disable 2nd while in _getkey
indicOnly:				EQU 2		;interrupt handler only checks run indicator

shiftFlags:				EQU 12h	;[2nd] and [ALPHA] flags
shift2nd:				EQU 3		;1=[2nd] has been pressed
shiftAlpha:				EQU 4		;1=[ALPHA] has been pressed
shiftLwrAlph:				EQU 5		;1=lower case, 0=upper case
shiftALock:				EQU 6		;1=alpha lock has been pressed
shiftKeepAlph:				EQU 7		;1=cannot cancel alpha shift
;----------------------------------------------------------------------
tblFlags:				EQU 13h	;table flags.
autoFill:				EQU 4		;1=prompt, 0=fillAuto
autoCalc:				EQU 5		;1=prompt, 0=CalcAuto
reTable:				EQU 6		;0=table is okay, 1=must recompute table.
;----------------------------------------------------------------------
sGrFlags:				EQU 14h
grfSplit:				EQU 0		;1=Split Graph, 0=Normal
vertSplit:				EQU 1		;1=Vertical (left-right) Split
grfSChanged:				EQU 2		;1=Graph just changed Split <-> normal
grfSplitOverride:				EQU 3		;1 = ignore graph split flag if set
write_on_graph:				EQU 4		;1 = TEXT OR EQU WRITING TO GRAPH SCREEN
g_style_active:				EQU 5		;1 = GRAPH STYLES ARE ENABLED, USE THEM
cmp_mod_box:				EQU 6		;1 = DOING MOD BOX PLOT COMPUTATION
textWrite:				EQU 7
;----------------------------------------------------------------------
newIndicFlags:				EQU 15h
extraIndic:				EQU 0
saIndic:				EQU 1
;3 has something to do with stat/list editor
;----------------------------------------------------------------------
interruptFlags:				EQU 16h
secondTimerEnabled:				EQU 0		;1 = second hardware timer enabled
batteryFlags:				EQU 16h
batteriesGood:				EQU 2		;1 = batteries good, I think this being reset will force the calc to shut off, used in USB error handler
;----------------------------------------------------------------------
smartFlags:				EQU 17h
smarter_mask:				EQU 3
smarter_test:				EQU 1
smartGraph:				EQU 0
smartGraph_inv:				EQU 1
;----------------------------------------------------------------------
traceFlags:				EQU 18h
grfExpr:				EQU 0		;set to hide expression while tracing
;----------------------------------------------------------------------
;There is a flag 19h.
;----------------------------------------------------------------------
statFlags2:				EQU 1Ah
statDiagnosticsOn:				EQU 0		;1 = stat diagnostics on
;----------------------------------------------------------------------
linkFlags:				EQU 1Bh
IDis95h:				EQU 1		;1 = link routines use machine ID 95h (CBL)
IDis82h:				EQU 2		;1 = link routines use machine ID 82h (TI-82)
IDis83h:				EQU 3		;1 = link routines use machine ID 83h (TI-83)
IDis03h:				EQU 4		;1 = link routines use machine ID 03h (PC to TI-83)
USBenabled:				EQU 5		;1 = use USB port first, or at least try to
;----------------------------------------------------------------------
;There is a flag 1Ch (stats-related).
;----------------------------------------------------------------------
;There is a flag 1Dh.
;----------------------------------------------------------------------
;There is a flag 1Eh.
;----------------------------------------------------------------------
varTypeMask:				EQU 1Fh	;is this a flag byte? yes
varGraphRef:				EQU 6
;----------------------------------------------------------------------
graphFlags2:				EQU 1Fh
splitOverride:				EQU 3		;0 = force full screen with ParseInp, or something
;----------------------------------------------------------------------
asm_Flag1:				EQU 21h	;ASM CODING
asm_Flag2:				EQU 22h	;ASM CODING
asm_Flag3:				EQU 23h	;ASM CODING
;----------------------------------------------------------------------
arcFlag:				EQU 24h
checkBatteryLevelFirst:				EQU 0		;1 = check battery levels in Arc_Unarc first and throw error if low

getSendFlg:				EQU 24h
comFailed:				EQU 1		;1 = Get/Send Communication Failed

selfTestFlag:				EQU 24h
resetOnPowerOn:				EQU 2		;1 = Force RAM reset when APD disabled on next power on

appLwrCaseFlag:				EQU 24h
lwrCaseActive:				EQU 3
;----------------------------------------------------------------------
contextFlags:				EQU 25h
nocxPutAway:				EQU 5		;1 = do not call cxPutAway routine
;----------------------------------------------------------------------
groupFlags:				EQU 26h ;used temporarily in Arc_Unarc
inGroup:				EQU 1		;1 = IN GROUP CONTEXT
noCompletionByte:				EQU 2		;1 = do not write 0FCh when calling Arc_Unarc, leave as 0FEh
noDataWrite:				EQU 3		;1 = do not write data when calling Arc_Unarc, nor size bytes
writeSizeBytesOnly:				EQU 5		;1 = only write size bytes when calling Arc_Unarc
;----------------------------------------------------------------------
;There is a flag 27h.
;----------------------------------------------------------------------
APIFlg:				EQU 28h
appAllowContext:				EQU 0           ;App wants context changes to happen
;1 set to ignore a key after returning from rawkeyhook
;2 set to send this key back?
;3
appRunning:				EQU 4		;app is currently running
;5
;6
appRetKeyOff:				EQU 7		;1 = GetKey returns kOff when [2nd]+[ON] pressed
;----------------------------------------------------------------------
apiFlg2:				EQU 29h
;----------------------------------------------------------------------
apiFlg3:				EQU 2Ah
;1 set means using small font?
;----------------------------------------------------------------------
apiFlg4:				EQU 2Bh
cellOverride:				EQU 1		;use cell override
fullScrnDraw:				EQU 2		;DRAW INTO LAST ROW/COL OF SCREEN
;----------------------------------------------------------------------
mouseFlag1:				EQU 2Ch
;----------------------------------------------------------------------
mouseFlag2:				EQU 2Dh ;might want to keep this always reset
;----------------------------------------------------------------------
xapFlag0:				EQU 2Eh	;external app flags, do not use 0,(iy+2Eh) (used by mouse routines)
xapFlag1:				EQU 2Fh
xapFlag2:				EQU 30h
xapFlag3:				EQU 31h
;----------------------------------------------------------------------
fontFlags:				EQU 32h
fracDrawLFont:				EQU 2
fracTallLFont:				EQU 3
customFont:				EQU 7
;----------------------------------------------------------------------
hookflags1:				EQU 33h ;also scriptFlag, rclFlag2, backGroundLink
alt_On:				EQU 0		;run ONSCRPT at startup
alt_Off:				EQU 1		;run OFFSCRPT at shutdown
useRclQueueEnd:				EQU 2		;1 = external mode
ignoreBPLink:				EQU 3		;1 = override flag for link activity hook
bPLinkOn:				EQU 4		;1 = link activity hook active
enableKeyEcho:				EQU 5		;1 = sends keypresses back to connected calc as remote control packets (with GetCSC vs. GetKey codes...really dumb, TI)
noTempDelete:				EQU 6		;1 = do not delete temporary programs at homescreen
;----------------------------------------------------------------------
hookflags2:				EQU 34h ;also sysHookFlg
getCSCHookActive:				EQU 0		;1 = GetCSC hook active
libraryHookActive:				EQU 1		;1 = library hook active
;2 This is set in the OS, but never referenced and the code is never executed
homescreenHookActive:				EQU 4		;1 = homescreen hook active
rawKeyHookActive:				EQU 5		;1 = raw key hook active
catalog2HookActive:				EQU 6		;1 = catalog 2 hook active
cursorHookActive:				EQU 7		;1 = cursor hook active
;----------------------------------------------------------------------
hookflags3:				EQU 35h ;also sysHookFlg1
tokenHookActive:				EQU 0		;1 = token hook active
localizeHookActive:				EQU 1		;1 = localize hook active
windowHookActive:				EQU 2		;1 = window hook active
graphHookActive:				EQU 3		;1 = graph hook active
yEquHookActive:				EQU 4		;1 = Y= hook active
fontHookActive:				EQU 5		;1 = font hook active
regraphHookActive:				EQU 6		;1 = regraph hook active
drawingHookActive:				EQU 7		;1 = drawing hook active
;----------------------------------------------------------------------
hookflags4:				EQU 36h ;also sysHookFlag2
traceHookActive:				EQU 0		;1 = trace hook active
parserHookActive:				EQU 1		;1 = parser hook active
appChangeHookActive:				EQU 2		;1 = app change hook active
catalog1HookActive:				EQU 3		;1 = catalog 1 hook active
helpHookActive:				EQU 4		;1 = help hook active
cxRedispHookActive:				EQU 5		;1 = cxRedisp hook active
menuHookActive:				EQU 6		;1 = menu hook active
silentLinkHookActive:				EQU 7		;1 = silent link hook active
;----------------------------------------------------------------------
;hookflags2Override	equ 37h		;set corresponding bit to kill iy+35h hook when executing app
;----------------------------------------------------------------------
;hookflags3Override	equ 38h		;set corresponding bit to kill iy+36h hook when executing app
;----------------------------------------------------------------------
;hookflags4Override	equ 39h		;set corresponding bit to kill iy+37h hook when executing app
;----------------------------------------------------------------------
;hookflags5		equ 3Ah
usbActivityHookActive:				EQU 0		;1 = USB activity hook active
;----------------------------------------------------------------------
plotFlag3:				EQU 3Ch  
bufferOnly:				EQU 0
useFastCirc:				EQU 4
;----------------------------------------------------------------------
dBKeyFlags:				EQU 3Dh
keyDefaultsF:				EQU 6		;1 = GetKey returns extended keycodes with TI-Keyboard
;----------------------------------------------------------------------
silentLinkFlags:				EQU 3Eh
silentLinkActive:				EQU 0		;1 = SE/84+ silent link is active

extraHookFlags:				EQU 3Eh
checkCatalog2HookVer:				EQU 3		;1 = check catalog 2 hook's version before executing it (and error or take other action if so)
openLibActive:				EQU 4		;1 = OpenLib( was successfully called on a Flash application (ExecLib will error if zero)
;5
;----------------------------------------------------------------------
clockFlags:				EQU 3Fh
notMDYMode:				EQU 0		;0 = M/D/Y format
isYMDMode:				EQU 1		;1 = Y/M/D format
is24Hour:				EQU 2		;1 = clock in 24 hour mode
inAfternoon:				EQU 3		;1 = current time is in afternoon (PM) (I think)
useTokensInString:				EQU 4		;1 = use tokens instead of characters when displaying clock as string (for getTmStr and getDtStr vs. MODE screen) (keep this reset)
displayClock:				EQU 5		;1 = display clock (this is set every second, reset otherwise)
clockOn:				EQU 6		;1 = clock on
;----------------------------------------------------------------------
USBFlag1:				EQU 40h
;1 Reset in I/O receiving code, doesn't seem to be used anywhere else
usbReceiveZone1:				EQU 2		;1 = receive to RAM pages 3/2 ("zone 1")
;----------------------------------------------------------------------
USBFlag2:				EQU 41h
;0
;2
noUSBHostInit:				EQU 3		;1 = _InitSpecificUSBDevice must fail
usbDataWaiting:				EQU 5
;6
;7
;----------------------------------------------------------------------
USBFlag3:				EQU 42h
;----------------------------------------------------------------------
USBFlag4:				EQU 43h
usbTimeoutError:				EQU 0		;1 = USB timeout error (crystal timer expired)
;----------------------------------------------------------------------

;Character Font Equates  
;-------------------------------
LrecurN:				EQU 001h
LrecurU:				EQU 002h
LrecurV:				EQU 003h
LrecurW:				EQU 004h
Lconvert:				EQU 005h
LsqUp:				EQU 006h
LsqDown:				EQU 007h
Lintegral:				EQU 008h
Lcross:				EQU 009h
LboxIcon:				EQU 00Ah
LcrossIcon:				EQU 00Bh
LdotIcon:				EQU 00Ch
LsubT:				EQU 00Dh ;small capital T for parametric mode.
LcubeR:				EQU 00Eh ;slightly different 3 for cubed root.
LhexF:				EQU 00Fh
Lroot:				EQU 010h
Linverse:				EQU 011h
Lsquare:				EQU 012h
Langle:				EQU 013h
Ldegree:				EQU 014h
Lradian:				EQU 015h
Ltranspose:				EQU 016h
LLE:				EQU 017h
LNE:				EQU 018h
LGE:				EQU 019h
Lneg:				EQU 01Ah
Lexponent:				EQU 01Bh
Lstore:				EQU 01Ch
Lten:				EQU 01Dh
LupArrow:				EQU 01Eh
LdownArrow:				EQU 01Fh
Lspace:				EQU 020h
Lexclam:				EQU 021h
Lquote:				EQU 022h
Lpound:				EQU 023h
Lfourth:				EQU 024h
Lpercent:				EQU 025h
Lampersand:				EQU 026h
Lapostrophe:				EQU 027h
LlParen:				EQU 028h
LrParen:				EQU 029h
Lasterisk:				EQU 02Ah
LplusSign:				EQU 02Bh
Lcomma:				EQU 02Ch
Ldash:				EQU 02Dh
Lperiod:				EQU 02Eh
Lslash:				EQU 02Fh
L0:				EQU 030h
L1:				EQU 031h
L2:				EQU 032h
L3:				EQU 033h
L4:				EQU 034h
L5:				EQU 035h
L6:				EQU 036h
L7:				EQU 037h
L8:				EQU 038h
L9:				EQU 039h
Lcolon:				EQU 03Ah
Lsemicolon:				EQU 03Bh
LLT:				EQU 03Ch
LEQ:				EQU 03Dh
LGT:				EQU 03Eh
Lquestion:				EQU 03Fh
LatSign:				EQU 040h
LcapA:				EQU 041h
LcapB:				EQU 042h
LcapC:				EQU 043h
LcapD:				EQU 044h
LcapE:				EQU 045h
LcapF:				EQU 046h
LcapG:				EQU 047h
LcapH:				EQU 048h
LcapI:				EQU 049h
LcapJ:				EQU 04Ah
LcapK:				EQU 04Bh
LcapL:				EQU 04Ch
LcapM:				EQU 04Dh
LcapN:				EQU 04Eh
LcapO:				EQU 04Fh
LcapP:				EQU 050h
LcapQ:				EQU 051h
LcapR:				EQU 052h
LcapS:				EQU 053h
LcapT:				EQU 054h
LcapU:				EQU 055h
LcapV:				EQU 056h
LcapW:				EQU 057h
LcapX:				EQU 058h
LcapY:				EQU 059h
LcapZ:				EQU 05Ah
Ltheta:				EQU 05Bh
Lbackslash:				EQU 05Ch
LrBrack:				EQU 05Dh
Lcaret:				EQU 05Eh
Lunderscore:				EQU 05Fh
Lbackquote:				EQU 060h
La:				EQU 061h
Lb:				EQU 062h
Lc:				EQU 063h
Ld:				EQU 064h
Le:				EQU 065h
Lf:				EQU 066h
Lg:				EQU 067h
Lh:				EQU 068h
Li:				EQU 069h
Lj:				EQU 06Ah
Lk:				EQU 06Bh
Ll:				EQU 06Ch
Lm:				EQU 06Dh
Ln:				EQU 06Eh
Lo:				EQU 06Fh
Lp:				EQU 070h
Lq:				EQU 071h
Lr:				EQU 072h
Ls:				EQU 073h
Lt:				EQU 074h
Lu:				EQU 075h
Lv:				EQU 076h
Lw:				EQU 077h
Lx:				EQU 078h
Ly:				EQU 079h
Lz:				EQU 07Ah
LlBrace:				EQU 07Bh
Lbar:				EQU 07Ch
LrBrace:				EQU 07Dh
Ltilde:				EQU 07Eh
LinvEQ:				EQU 07Fh
Lsub0:				EQU 080h
Lsub1:				EQU 081h
Lsub2:				EQU 082h
Lsub3:				EQU 083h
Lsub4:				EQU 084h
Lsub5:				EQU 085h
Lsub6:				EQU 086h
Lsub7:				EQU 087h
Lsub8:				EQU 088h
Lsub9:				EQU 089h
LcapAAcute:				EQU 08Ah
LcapAGrave:				EQU 08Bh
LcapACaret:				EQU 08Ch
LcapADier:				EQU 08Dh
LaAcute:				EQU 08Eh
LaGrave:				EQU 08Fh
LaCaret:				EQU 090h
LaDier:				EQU 091h
LcapEAcute:				EQU 092h
LcapEGrave:				EQU 093h
LcapECaret:				EQU 094h
LcapEDier:				EQU 095h
LeAcute:				EQU 096h
LeGrave:				EQU 097h
LeCaret:				EQU 098h
LeDier:				EQU 099h
LcapIAcute:				EQU 09Ah
LcapIGrave:				EQU 09Bh
LcapICaret:				EQU 09Ch
LcapIDier:				EQU 09Dh
LiAcute:				EQU 09Eh
LiGrave:				EQU 09Fh
LiCaret:				EQU 0A0h
LiDier:				EQU 0A1h
LcapOAcute:				EQU 0A2h
LcapOGrave:				EQU 0A3h
LcapOCaret:				EQU 0A4h
LcapODier:				EQU 0A5h
LoAcute:				EQU 0A6h
LoGrave:				EQU 0A7h
LoCaret:				EQU 0A8h
LoDier:				EQU 0A9h
LcapUAcute:				EQU 0AAh
LcapUGrave:				EQU 0ABh
LcapUCaret:				EQU 0ACh
LcapUDier:				EQU 0ADh
LuAcute:				EQU 0AEh
LuGrave:				EQU 0AFh
LuCaret:				EQU 0B0h
LuDier:				EQU 0B1h
LcapCCed:				EQU 0B2h
LcCed:				EQU 0B3h
LcapNTilde:				EQU 0B4h
LnTilde:				EQU 0B5h
Laccent:				EQU 0B6h
Lgrave:				EQU 0B7h
Ldieresis:				EQU 0B8h
LquesDown:				EQU 0B9h
LexclamDown:				EQU 0BAh
Lalpha:				EQU 0BBh
Lbeta:				EQU 0BCh
Lgamma:				EQU 0BDh
LcapDelta:				EQU 0BEh
Ldelta:				EQU 0BFh
Lepsilon:				EQU 0C0h
LlBrack:				EQU 0C1h
Llambda:				EQU 0C2h
Lmu:				EQU 0C3h
Lpi:				EQU 0C4h
Lrho:				EQU 0C5h
LcapSigma:				EQU 0C6h
Lsigma:				EQU 0C7h
Ltau:				EQU 0C8h
Lphi:				EQU 0C9h
LcapOmega:				EQU 0CAh
LxMean:				EQU 0CBh
LyMean:				EQU 0CCh
LsupX:				EQU 0CDh
Lellipsis:				EQU 0CEh
Lleft:				EQU 0CFh
Lblock:				EQU 0D0h
Lper:				EQU 0D1h
Lhyphen:				EQU 0D2h
Larea:				EQU 0D3h
Ltemp:				EQU 0D4h
Lcube:				EQU 0D5h
Lenter:				EQU 0D6h
LimagI:				EQU 0D7h
Lphat:				EQU 0D8h
Lchi:				EQU 0D9h
LstatF:				EQU 0DAh
Llne:				EQU 0DBh
LlistL:				EQU 0DCh
LfinanN:				EQU 0DDh
L2_r_paren:				EQU 0DEh
LblockArrow:				EQU 0DFh
LcurO:				EQU 0E0h
LcurO2:				EQU 0E1h
LcurOcapA:				EQU 0E2h
LcurOa:				EQU 0E3h
LcurI:				EQU 0E4h
LcurI2:				EQU 0E5h
LcurIcapA:				EQU 0E6h
LcurIa:				EQU 0E7h
LGline:				EQU 0E8h ; = 0
LGthick:				EQU 0E9h ; = 1
LGabove:				EQU 0EAh ; = 2
LGbelow:				EQU 0EBh ; = 3
LGpath:				EQU 0ECh ; = 4
LGanimate:				EQU 0EDh ; = 5
LGdot:				EQU 0EEh ; = 6
LUpBlk:				EQU 0EFh ;Up arrow and Block in solver
LDnBlk:				EQU 0F0h ;Down arrow and Block in solver
LcurFull:				EQU 0F1h ;note: must be last char (PutMap checks)

;(MenuCurrent) Values
;--------------------------
mConfirmation:				EQU 01h
mApps:				EQU 02h
mProgramHome:				EQU 03h
mPrgm_Run:				EQU 00h
mPrgm_Edit:				EQU 01h
mPrgm_New:				EQU 02h
mZoom:				EQU 04h
mZoom_Zoom:				EQU 00h
mZoom_Memory:				EQU 01h
mDraw:				EQU 05h
mDraw_Draw:				EQU 00h
mDraw_Points:				EQU 01h
mDraw_Store:				EQU 02h
mStatPlots:				EQU 06h
mStat:				EQU 07h
mStat_Edit:				EQU 00h
mStat_Calc:				EQU 01h
mStat_Tests:				EQU 02h
mMath:				EQU 08h
mMath_Math:				EQU 00h
mMath_Number:				EQU 01h
mMath_Complex:				EQU 02h
mMath_Prob:				EQU 03h
mTest:				EQU 09h
mTest_Test:				EQU 00h
mTest_Logic:				EQU 01h
mVars:				EQU 0Ah
mVars_Vars:				EQU 00h
mVars_YVars:				EQU 01h
mMemory:				EQU 0Ch
mMatrix:				EQU 0Dh
mMatrix_Name:				EQU 00h
mMatrix_Math:				EQU 01h
mMatrix_Edit:				EQU 02h
mDistr:				EQU 0Eh
mDistr_Distr:				EQU 00h
mDistr_Draw:				EQU 01h
mAngle:				EQU 0Fh
mList:				EQU 10h
mList_Names:				EQU 00h
mList_Ops:				EQU 01h
mList_Math:				EQU 02h
mCalculate:				EQU 11h
mVarsWin:				EQU 14h
mVarsWin_XY:				EQU 00h
mVarsWin_TTh:				EQU 01h
mVarsWin_UVW:				EQU 02h
mVarsZoom:				EQU 15h
mVarsZoom_ZXY:				EQU 00h
mVarsZoom_ZT:				EQU 01h
mVarsZoom_UVW:				EQU 02h
mVarsGDB:				EQU 16h
mVarsPics:				EQU 17h
mVarsStrings:				EQU 18h
mVarsStat:				EQU 19h
mVarsStat_XY:				EQU 00h
mVarsStat_Sigma:				EQU 01h
mVarsStat_EQ:				EQU 02h
mVarsStat_Test:				EQU 03h
mVarsStat_Pts:				EQU 04h
mVarsTable:				EQU 1Ah
mVarsYEqu:				EQU 1Bh
mVarsParametric:				EQU 1Ch
mVarsPolar:				EQU 1Dh
mVarsFnOnOff:				EQU 1Eh
mMemReset:				EQU 1Fh
mMemReset_RAM:				EQU 00h
mMemReset_ROM:				EQU 01h
mMemReset_All:				EQU 02h
mMemMgmtDel:				EQU 20h
mMemResetDefaults:				EQU 21h
mMemResetRAMAll:				EQU 23h
mMemResetROMVars:				EQU 24h
mMemResetROMApps:				EQU 25h
mMemResetROMAll:				EQU 26h
mMemResetAll:				EQU 27h
mGroup:				EQU 28h
mGroup_New:				EQU 00h
mGroup_Ungroup:				EQU 01h
mGroupVars:				EQU 29h
mProgramEdit:				EQU 2Ah
mPrgmEd_Ctrl:				EQU 00h
mPrgmEd_IO:				EQU 01h
mPrgmEd_Exec:				EQU 02h
mPrgmZoom:				EQU 2Bh
mPrgmZoom_Zoom:				EQU 00h
mPrgmZoom_Mem:				EQU 01h
mPrgmDraw:				EQU 2Ch
mPrgmDraw_Draw:				EQU 00h
mPrgmDraw_Pt:				EQU 01h
mPrgmDraw_Store:				EQU 02h
mPrgmStatPlot:				EQU 2Dh
mPrgmSP_Plots:				EQU 00h
mPrgmSP_Type:				EQU 01h
mPrgmSP_Mark:				EQU 02h
mPrgmStat:				EQU 2Eh
mPrgmStat_Edit:				EQU 00h
mPrgmStat_Calc:				EQU 01h
mPrgmStat_Tests:				EQU 02h
mPrgmMath:				EQU 2Fh
mPrgmMath_Math:				EQU 00h
mPrgmMath_Num:				EQU 01h
mPrgmMath_Cplx:				EQU 02h
mPrgmMath_Prob:				EQU 03h
mLink:				EQU 30h
mLink_Send:				EQU 00h
mLink_Recieve:				EQU 01h
mLinkTrasmit:				EQU 31h
mLinkXmit_Xmit:				EQU 01h
mGarbageCollect:				EQU 3Fh
mSelectGroupVars:				EQU 40h
mSelGrpVars_Sel:				EQU 00h
mSelGrpVars_Grp:				EQU 01h
mDuplicateName:				EQU 42h
mCatalog:				EQU 45h
mFinance:				EQU 22h
mFinance_Calc:				EQU 00h
mFinance_Vars:				EQU 01h

;Keypress Equates
;-------------------------------
kRight:				EQU 001h
kLeft:				EQU 002h
kUp:				EQU 003h
kDown:				EQU 004h
kEnter:				EQU 005h
kAlphaEnter:				EQU 006h
kAlphaUp:				EQU 007h
kAlphaDown:				EQU 008h
kClear:				EQU 009h
kDel:				EQU 00Ah
kIns:				EQU 00Bh
kRecall:				EQU 00Ch
kLastEnt:				EQU 00Dh
kBOL:				EQU 00Eh
kEOL:				EQU 00Fh

kSelAll:				EQU 010h
kUnselAll:				EQU 011h
kLtoTI82:				EQU 012h
kBackup:				EQU 013h
kRecieve:				EQU 014h
kLnkQuit:				EQU 015h
kTrans:				EQU 016h
kRename:				EQU 017h
kOverw:				EQU 018h
kOmit:				EQU 019h
kCont:				EQU 01Ah
kSendID:				EQU 01Bh
kSendSW:				EQU 01Ch
kYes:				EQU 01Dh
kNoWay:				EQU 01Eh
kvSendType:				EQU 01Fh
kOverWAll:				EQU 020h
kNo:				EQU 025h
kKReset:				EQU 026h
kApp:				EQU 027h
kDoug:				EQU 028h
kListflag:				EQU 029h
menuStart:				EQU 02Bh
kAreYouSure:				EQU 02Bh
kAppsMenu:				EQU 02Ch
kPrgm:				EQU 02Dh
kZoom:				EQU 02Eh
kDraw:				EQU 02Fh
kSPlot:				EQU 030h
kStat:				EQU 031h
kMath:				EQU 032h
kTest:				EQU 033h
kChar:				EQU 034h
kVars:				EQU 035h
kMem:				EQU 036h
kMatrix:				EQU 037h
kDist:				EQU 038h
kAngle:				EQU 039h
kList:				EQU 03Ah
kCalc:				EQU 03Bh
kFin:				EQU 03Ch
menuEnd:				EQU kFin
kCatalog:				EQU 03Eh
kInputDone:				EQU 03Fh
kOff:				EQU kInputDone
kQuit:				EQU 040h
appStart:				EQU kQuit
kLinkIO:				EQU 041h
kMatrixEd:				EQU 042h
kStatEd:				EQU 043h
kGraph:				EQU 044h
kMode:				EQU 045h
kPrgmEd:				EQU 046h ;PROGRAM EDIT
kPrgmCr:				EQU 047h ;PROGRAM CREATE
kWindow:				EQU 048h ;RANGE EDITOR
kYequ:				EQU 049h ;EQUATION EDITOR
kTable:				EQU 04Ah ;TABLE EDITOR
kTblSet:				EQU 04Bh ;TABLE SET
kChkRAM:				EQU 04Ch ;CHECK RAM (About screen)
kDelMem:				EQU 04Dh ;DELETE MEM
kResetMem:				EQU 04Eh ;RESET MEM
kResetDef:				EQU 04Fh ;RESET DEFAULT
kPrgmInput:				EQU 050h ;PROGRAM INPUT
kZFactEd:				EQU 051h ;ZOOM FACTOR EDITOR
kError:				EQU 052h ;ERROR
kSolveTVM:				EQU 053h ;TVM SOLVER
kSolveRoot:				EQU 054h ;SOLVE EDITOR
kStatP:				EQU 055h ;stat plot
kInfStat:				EQU 056h ;Inferential Statistic
kFormat:				EQU 057h ;FORMAT
kExtApps:				EQU 058h ;External Applications.     NEW
kNewApps:				EQU 059h ;New Apps for Cerberus.
append:				EQU kNewApps
echoStart1:				EQU 05Ah
kTrace:				EQU 05Ah
kZFit:				EQU 05Bh
kZIn:				EQU 05Ch
kZOut:				EQU 05Dh
kZPrev:				EQU 05Eh
kBox:				EQU 05Fh
kDecml:				EQU 060h
kSetZm:				EQU 061h
kSquar:				EQU 062h
kStd:				EQU 063h
kTrig:				EQU 064h
kUsrZm:				EQU 065h
kZSto:				EQU 066h
kZInt:				EQU 067h
kZStat:				EQU 068h
echoStart2:				EQU 069h
kSelect:				EQU 069h
kCircl:				EQU 06Ah
kClDrw:				EQU 06Bh
kLine:				EQU 06Ch
kPen:				EQU 06Dh
kPtChg:				EQU 06Eh
kPtOff:				EQU 06Fh
kPtOn:				EQU 070h
kVert:				EQU 071h
kHoriz:				EQU 072h
kText:				EQU 073h
kTanLn:				EQU 074h
kEval:				EQU 075h
kInters:				EQU 076h
kDYDX:				EQU 077h
kFnIntg:				EQU 078h
kRootG:				EQU 079h
kDYDT:				EQU 07Ah
kDXDT:				EQU 07Bh
kDRDo:				EQU 07Ch
KGFMin:				EQU 07Dh
KGFMax:				EQU 07Eh
EchoStart:				EQU 07Fh
kListName:				EQU 07Fh
kAdd:				EQU 080h
kSub:				EQU 081h
kMul:				EQU 082h
kDiv:				EQU 083h
kExpon:				EQU 084h
kLParen:				EQU 085h
kRParen:				EQU 086h
kLBrack:				EQU 087h
kRBrack:				EQU 088h
kShade:				EQU 089h
kStore:				EQU 08Ah
kComma:				EQU 08Bh
kChs:				EQU 08Ch
kDecPnt:				EQU 08Dh
k0:				EQU 08Eh
k1:				EQU 08Fh
k2:				EQU 090h
k3:				EQU 091h
k4:				EQU 092h
k5:				EQU 093h
k6:				EQU 094h
k7:				EQU 095h
k8:				EQU 096h
k9:				EQU 097h
kEE:				EQU 098h
kSpace:				EQU 099h
kCapA:				EQU 09Ah
kCapB:				EQU 09Bh
kCapC:				EQU 09Ch
kCapD:				EQU 09Dh
kCapE:				EQU 09Eh
kCapF:				EQU 09Fh
kCapG:				EQU 0A0h
kCapH:				EQU 0A1h
kCapI:				EQU 0A2h
kCapJ:				EQU 0A3h
kCapK:				EQU 0A4h
kCapL:				EQU 0A5h
kCapM:				EQU 0A6h
kCapN:				EQU 0A7h
kCapO:				EQU 0A8h
kCapP:				EQU 0A9h
kCapQ:				EQU 0AAh
kCapR:				EQU 0ABh
kCapS:				EQU 0ACh
kCapT:				EQU 0ADh
kCapU:				EQU 0AEh
kCapV:				EQU 0AFh
kCapW:				EQU 0B0h
kCapX:				EQU 0B1h
kCapY:				EQU 0B2h
kCapZ:				EQU 0B3h
kVarx:				EQU 0B4h
kPi:				EQU 0B5h
kInv:				EQU 0B6h
kSin:				EQU 0B7h
kASin:				EQU 0B8h
kCos:				EQU 0B9h
kACos:				EQU 0BAh
kTan:				EQU 0BBh
kATan:				EQU 0BCh
kSquare:				EQU 0BDh
kSqrt:				EQU 0BEh
kLn:				EQU 0BFh
kExp:				EQU 0C0h
kLog:				EQU 0C1h
kALog:				EQU 0C2h
kToABC:				EQU 0C3h
kClrTbl:				EQU 0C4h
kAns:				EQU 0C5h
kColon:				EQU 0C6h
kNDeriv:				EQU 0C7h
kFnInt:				EQU 0C8h
kRoot:				EQU 0C9h
kQuest:				EQU 0CAh
kQuote:				EQU 0CBh
kTheta:				EQU 0CCh
kIf:				EQU 0CDh
kThen:				EQU 0CEh
kElse:				EQU 0CFh
kFor:				EQU 0D0h
kWhile:				EQU 0D1h
kRepeat:				EQU 0D2h
kEnd:				EQU 0D3h
kPause:				EQU 0D4h
kLbl:				EQU 0D5h
kGoto:				EQU 0D6h
kISG:				EQU 0D7h
kDSL:				EQU 0D8h
kMenu:				EQU 0D9h
kExec:				EQU 0DAh
kReturn:				EQU 0DBh
kStop:				EQU 0DCh
kInput:				EQU 0DDh
kPrompt:				EQU 0DEh
kDisp:				EQU 0DFh
kDispG:				EQU 0E0h
kDispT:				EQU 0E1h
kOutput:				EQU 0E2h
kGetKey:				EQU 0E3h
kClrHome:				EQU 0E4h
kPrtScr:				EQU 0E5h
kSinH:				EQU 0E6h
kCosH:				EQU 0E7h
kTanH:				EQU 0E8h
kASinH:				EQU 0E9h
kACosH:				EQU 0EAh
kATanH:				EQU 0EBh
kLBrace:				EQU 0ECh
kRBrace:				EQU 0EDh
kI:				EQU 0EEh
kCONSTeA:				EQU 0EFh
kPlot3:				EQU 0F0h
kFMin:				EQU 0F1h
kFMax:				EQU 0F2h
kL1A:				EQU 0F3h
kL2A:				EQU 0F4h
kL3A:				EQU 0F5h
kL4A:				EQU 0F6h
kL5A:				EQU 0F7h
kL6A:				EQU 0F8h
kunA:				EQU 0F9h
kvnA:				EQU 0FAh
kwnA:				EQU 0FBh

;THIS KEY MEANS THAT IT IS A 2 BYTE KEYCODE
;THERE ARE 2 OF THESE KEYS; BE CAREFUL WITH USAGE
;------------------------------------------------
kExtendEcho2:				EQU 0FCh

;THIS KEY MEANS THAT THE KEY PRESS IS ONE THAT ECHOS
;INTO A BUFFER, AND IT IS A 2 BYTE KEY CODE, GO LOOK AT
;(EXTECHO) FOR THE KEY VALUE 
;------------------------------------------------------
kExtendEcho:				EQU 0FEh

kE1BT:				EQU 0
kDrawInv:				EQU kE1BT
kDrawF:				EQU kE1BT+1
kPixelOn:				EQU kE1BT+2
kPixelOff:				EQU kE1BT+3
kPxlTest:				EQU kE1BT+4
kRCGDB:				EQU kE1BT+5
kRCPic:				EQU kE1BT+6
kSTGDB:				EQU kE1BT+7
kSTPic:				EQU kE1BT+8
kAbs:				EQU kE1BT+9
kTEqu:				EQU kE1BT+10 ;==
kTNoteQ:				EQU kE1BT+11 ;<>
kTGT:				EQU kE1BT+12 ;>
kTGTE:				EQU kE1BT+13 ;>=
kTLT:				EQU kE1BT+14 ;<
kTLTE:				EQU kE1BT+15 ;<=
kAnd:				EQU kE1BT+16
kOr:				EQU kE1BT+17
kXor:				EQU kE1BT+18
kNot:				EQU kE1BT+19
kLR1:				EQU kE1BT+20
kXRoot:				EQU kE1BT+21
kCube:				EQU kE1BT+22
kCbRt:				EQU kE1BT+23 ;Cube ROOT
kToDec:				EQU kE1BT+24
kCubicR:				EQU kE1BT+25
kQuartR:				EQU kE1BT+26
kPlot1:				EQU kE1BT+27
kPlot2:				EQU kE1BT+28
kRound:				EQU kE1BT+29
kIPart:				EQU kE1BT+30
kFPart:				EQU kE1BT+31
kInt:				EQU kE1BT+32
kRand:				EQU kE1BT+33
kNPR:				EQU kE1BT+34
kNCR:				EQU kE1BT+35
kXFactorial:				EQU kE1BT+36
kRad:				EQU kE1BT+37
kDegr:				EQU kE1BT+38 ;DEGREES CONV
kAPost:				EQU kE1BT+39
kToDMS:				EQU kE1BT+40
kRToPo:				EQU kE1BT+41 ;R
kRToPr:				EQU kE1BT+42
kPToRx:				EQU kE1BT+43
kPToRy:				EQU kE1BT+44
kRowSwap:				EQU kE1BT+45
kRowPlus:				EQU kE1BT+46
kTimRow:				EQU kE1BT+47
kTRowP:				EQU kE1BT+48
kSortA:				EQU kE1BT+49
kSortD:				EQU kE1BT+50
kSeq:				EQU kE1BT+51
kMin:				EQU kE1BT+52
kMax:				EQU kE1BT+53
kMean:				EQU kE1BT+54
kMedian:				EQU kE1BT+55
kSum:				EQU kE1BT+56
kProd:				EQU kE1BT+57
kDet:				EQU kE1BT+58
kTransp:				EQU kE1BT+59
kDim:				EQU kE1BT+60
kFill:				EQU kE1BT+61
kIdent:				EQU kE1BT+62
kRandm:				EQU kE1BT+63
kAug:				EQU kE1BT+64
kOneVar:				EQU kE1BT+65
kTwoVar:				EQU kE1BT+66
kLR:				EQU kE1BT+67
kLRExp:				EQU kE1BT+68
kLRLn:				EQU kE1BT+69
kLRPwr:				EQU kE1BT+70
kMedMed:				EQU kE1BT+71
kQuad:				EQU kE1BT+72
kClrLst:				EQU kE1BT+73
kHist:				EQU kE1BT+74
kxyLine:				EQU kE1BT+75
kScatter:				EQU kE1BT+76
kmRad:				EQU kE1BT+77
kmDeg:				EQU kE1BT+78
kmNormF:				EQU kE1BT+79
kmSci:				EQU kE1BT+80
kmEng:				EQU kE1BT+81
kmFloat:				EQU kE1BT+82
kFix:				EQU kE1BT+83
kSplitOn:				EQU kE1BT+84
kFullScreen:				EQU kE1BT+85
kStndrd:				EQU kE1BT+86
kParam:				EQU kE1BT+87
kPolar:				EQU kE1BT+88
kSeqG:				EQU kE1BT+89
kAFillOn:				EQU kE1BT+90
kAFillOff:				EQU kE1BT+91
kACalcOn:				EQU kE1BT+92
kACalcOff:				EQU kE1BT+93
kFNOn:				EQU kE1BT+94
kFNOff:				EQU kE1BT+95
kPlotsOn:				EQU kE1BT+96
kPlotsOff:				EQU kE1BT+97
kPixelChg:				EQU kE1BT+98
kSendMBL:				EQU kE1BT+99
kRecvMBL:				EQU kE1BT+100
kBoxPlot:				EQU kE1BT+101
kBoxIcon:				EQU kE1BT+102
kCrossIcon:				EQU kE1BT+103
kDotIcon:				EQU kE1BT+104
kE2BT:				EQU kE1BT+105
kSeqential:				EQU kE2BT
kSimulG:				EQU kE2BT+1
kPolarG:				EQU kE2BT+2
kRectG:				EQU kE2BT+3
kCoordOn:				EQU kE2BT+4
kCoordOff:				EQU kE2BT+5
kDrawLine:				EQU kE2BT+6
kDrawDot:				EQU kE2BT+7
kAxisOn:				EQU kE2BT+8
kAxisOff:				EQU kE2BT+9
kGridOn:				EQU kE2BT+10
kGridOff:				EQU kE2BT+11
kLblOn:				EQU kE2BT+12
kLblOff:				EQU kE2BT+13
kL1:				EQU kE2BT+14
kL2:				EQU kE2BT+15
kL3:				EQU kE2BT+16
kL4:				EQU kE2BT+17
kL5:				EQU kE2BT+18
kL6:				EQU kE2BT+19

;These keys are laid on top of existing keys to
;enable localization in the inferential stats editor.
;----------------------------------------------------
kinfData:				EQU kL1
kinfStats:				EQU kL1+1
kinfYes:				EQU kL1+2
kinfNo:				EQU kL1+3
kinfCalc:				EQU kL1+4
kinfDraw:				EQU kL1+5
kinfAlt1ne:				EQU kL1+6
kinfAlt1lt:				EQU kL1+7
kinfAlt1gt:				EQU kL1+8
kinfAlt2ne:				EQU kL1+9
kinfAlt2lt:				EQU kL1+10
kinfAlt2gt:				EQU kL1+11
kinfAlt3ne:				EQU kL1+12
kinfAlt3lt:				EQU kL1+13
kinfAlt3gt:				EQU kL1+14
kinfAlt4ne:				EQU kL1+15
kinfAlt4lt:				EQU kL1+16
kinfAlt4gt:				EQU kL1+17
kinfAlt5ne:				EQU kL1+18
kinfAlt5lt:				EQU kL1+19
kinfAlt5gt:				EQU kL1+20
kinfAlt6ne:				EQU kL1+21
kinfAlt6lt:				EQU kL1+22
kinfAlt6gt:				EQU kL1+23
kMatA:				EQU kE2BT+20
kMatB:				EQU kE2BT+21
kMatC:				EQU kE2BT+22
kMatD:				EQU kE2BT+23
kMatE:				EQU kE2BT+24
kXmin:				EQU kE2BT+25
kXmax:				EQU kE2BT+26
kXscl:				EQU kE2BT+27
kYmin:				EQU kE2BT+28
kYmax:				EQU kE2BT+29
kYscl:				EQU kE2BT+30
kTmin:				EQU kE2BT+31
kTmax:				EQU kE2BT+32
kTStep:				EQU kE2BT+33
kOmin:				EQU kE2BT+34
kOmax:				EQU kE2BT+35
kOStep:				EQU kE2BT+36
ku0:				EQU kE2BT+37
kv0:				EQU kE2BT+38
knMin:				EQU kE2BT+39
knMax:				EQU kE2BT+40
kDeltaY:				EQU kE2BT+41
kDeltaX:				EQU kE2BT+42
kZXmin:				EQU kE2BT+43
kZXmax:				EQU kE2BT+44
kZXscl:				EQU kE2BT+45
kZYmin:				EQU kE2BT+46
kZYmax:				EQU kE2BT+47
kZYscl:				EQU kE2BT+48
kZTmin:				EQU kE2BT+49
kZTmax:				EQU kE2BT+50
kZTStep:				EQU kE2BT+51
kZOmin:				EQU kE2BT+52
kZOmax:				EQU kE2BT+53
kZOStep:				EQU kE2BT+54
kZu0:				EQU kE2BT+55
kZv0:				EQU kE2BT+56
kZnMin:				EQU kE2BT+57
kZnMax:				EQU kE2BT+58
kDelLast:				EQU kE2BT+59
kSinReg:				EQU kE2BT+60
kConstE:				EQU kE2BT+61
kPic1:				EQU kE2BT+62
kPic2:				EQU kE2BT+63
kPic3:				EQU kE2BT+64
kDelVar:				EQU kE2BT+65
kGetCalc:				EQU kE2BT+66
kRealM:				EQU kE2BT+67
kPolarM:				EQU kE2BT+68
kRectM:				EQU kE2BT+69
kuv:				EQU kE2BT+70 ;U vs V
kvw:				EQU kE2BT+71 ;V vs W
kuw:				EQU kE2BT+72 ;U vs W
kFinPMTend:				EQU kE2BT+73
kFinPMTbeg:				EQU kE2BT+74
kGraphStyle:				EQU kE2BT+75
kExprOn:				EQU kE2BT+76
kExprOff:				EQU kE2BT+77
kStatA:				EQU kE2BT+78
kStatB:				EQU kE2BT+79
kStatC:				EQU kE2BT+80
kCorr:				EQU kE2BT+81
kStatD:				EQU kE2BT+82
kStatE:				EQU kE2BT+83
kRegEq:				EQU kE2BT+84
kMinX:				EQU kE2BT+85
kQ1:				EQU kE2BT+86
kMD:				EQU kE2BT+87
kQ3:				EQU kE2BT+88
kMaxX:				EQU kE2BT+89
kStatX1:				EQU kE2BT+90
kStatY1:				EQU kE2BT+91
kStatX2:				EQU kE2BT+92
kStatY2:				EQU kE2BT+93
kStatX3:				EQU kE2BT+94
kStatY3:				EQU kE2BT+95
kTblMin:				EQU kE2BT+96
kTblStep:				EQU kE2BT+97
kSetupLst:				EQU kE2BT+98
kClrAllLst:				EQU kE2BT+99
kLogistic:				EQU kE2BT+100
kZTest:				EQU kE2BT+101
kTTest:				EQU kE2BT+102
k2SampZTest:				EQU kE2BT+103
k2SampTTest:				EQU kE2BT+104
k1PropZTest:				EQU kE2BT+105
k2PropZTest:				EQU kE2BT+106
kChiTest:				EQU kE2BT+107
k2SampFTest:				EQU kE2BT+108
kZIntVal:				EQU kE2BT+109
kTIntVal:				EQU kE2BT+110
k2SampTInt:				EQU kE2BT+111
k2SampZInt:				EQU kE2BT+112
k1PropZInt:				EQU kE2BT+113
k2PropZInt:				EQU kE2BT+114
kDNormal:				EQU kE2BT+115
kInvNorm:				EQU kE2BT+116
kDT:				EQU kE2BT+117
kChi:				EQU kE2BT+118
kDF:				EQU kE2BT+119
kBinPDF:				EQU kE2BT+120
kBinCDF:				EQU kE2BT+121
kPoiPDF:				EQU kE2BT+122
kPoiCDF:				EQU kE2BT+123
kun:				EQU kE2BT+124
kvn:				EQU kE2BT+125
kwn:				EQU kE2BT+126
kRecn:				EQU kE2BT+127
kPlotStart:				EQU kE2BT+128
kZPlotStart:				EQU kE2BT+129 ;recursion n
kXFact:				EQU kE2BT+130 ;PlotStart
kYFact:				EQU kE2BT+131 ;ZPlotStart
kANOVA:				EQU kE2BT+132 ;XFact
kMaxY:				EQU kE2BT+133 ;YFact
kWebOn:				EQU kE2BT+134 ;MinY
kWebOff:				EQU kE2BT+135 ;MaxY
kTblInput:				EQU kE2BT+136 ;WEB ON
kGeoPDF:				EQU kE2BT+137 ;WEB OFF
kGeoCDF:				EQU kE2BT+138 ;WEB OFF
kShadeNorm:				EQU kE2BT+139
kShadeT:				EQU kE2BT+140
kShadeChi:				EQU kE2BT+141
kShadeF:				EQU kE2BT+142
kPlotStep:				EQU kE2BT+143
kZPlotStep:				EQU kE2BT+144
kLinRegtTest:				EQU kE2BT+145
KMGT:				EQU kE2BT+146 ;VERT SPLIT
kSelectA:				EQU kE2BT+147
kZFitA:				EQU kE2BT+148
kE2BT_End:				EQU kZFitA

;More 2 Byte Keys 
;------------------------------------
kE2BT2:				EQU 0
kGDB1:				EQU kE2BT2
kGDB2:				EQU kE2BT2+1
kGDB3:				EQU kE2BT2+2
kY1:				EQU kE2BT2+3
kY2:				EQU kE2BT2+4
kY3:				EQU kE2BT2+5
kY4:				EQU kE2BT2+6
kY5:				EQU kE2BT2+7
kY6:				EQU kE2BT2+8
kY7:				EQU kE2BT2+9
kY8:				EQU kE2BT2+10
kY9:				EQU kE2BT2+11
kY0:				EQU kE2BT2+12
kX1T:				EQU kE2BT2+13
kY1T:				EQU kE2BT2+14
kX2T:				EQU kE2BT2+15
kY2T:				EQU kE2BT2+16
kX3T:				EQU kE2BT2+17
kY3T:				EQU kE2BT2+18
kX4T:				EQU kE2BT2+19
kY4T:				EQU kE2BT2+20
kX5T:				EQU kE2BT2+21
kY5T:				EQU kE2BT2+22
kX6T:				EQU kE2BT2+23
kY6T:				EQU kE2BT2+24
kR1:				EQU kE2BT2+25
kR2:				EQU kE2BT2+26
kR3:				EQU kE2BT2+27
kR4:				EQU kE2BT2+28
kR5:				EQU kE2BT2+29
kR6:				EQU kE2BT2+30
kGDB4:				EQU kE2BT2+31
kGDB5:				EQU kE2BT2+32
kGDB6:				EQU kE2BT2+33
kPic4:				EQU kE2BT2+34
kPic5:				EQU kE2BT2+35
kPic6:				EQU kE2BT2+36
kGDB7:				EQU kE2BT2+37
kGDB8:				EQU kE2BT2+38
kGDB9:				EQU kE2BT2+39
kGDB0:				EQU kE2BT2+40
kPic7:				EQU kE2BT2+41
kPic8:				EQU kE2BT2+42
kPic9:				EQU kE2BT2+43
kPic0:				EQU kE2BT2+44
kStatN:				EQU kE2BT2+45
kXMean:				EQU kE2BT2+46
kConj:				EQU kE2BT2+47
kReal:				EQU kE2BT2+48
kFAngle:				EQU kE2BT2+49
kLCM:				EQU kE2BT2+50
kGCD:				EQU kE2BT2+51
kRandInt:				EQU kE2BT2+52
kRandNorm:				EQU kE2BT2+53
kToPolar:				EQU kE2BT2+54
kToRect:				EQU kE2BT2+55
kYMean:				EQU kE2BT2+56
kStdX:				EQU kE2BT2+57
kStdX1:				EQU kE2BT2+58
kw0:				EQU kE2BT2+59
kMatF:				EQU kE2BT2+60
kMatG:				EQU kE2BT2+61
kMatRH:				EQU kE2BT2+62
kMatI:				EQU kE2BT2+63
kMatJ:				EQU kE2BT2+64
kYMean1:				EQU kE2BT2+65
kStdY:				EQU kE2BT2+66
kStdY1:				EQU kE2BT2+67
kMatToLst:				EQU kE2BT2+68
kLstToMat:				EQU kE2BT2+69
kCumSum:				EQU kE2BT2+70
kDeltaLst:				EQU kE2BT2+71
kStdDev:				EQU kE2BT2+72
kVariance:				EQU kE2BT2+73
kLength:				EQU kE2BT2+74
kEquToStrng:				EQU kE2BT2+75
kStrngToEqu:				EQU kE2BT2+76
kExpr:				EQU kE2BT2+77
kSubStrng:				EQU kE2BT2+78
kInStrng:				EQU kE2BT2+79
kStr1:				EQU kE2BT2+80
kStr2:				EQU kE2BT2+81
kStr3:				EQU kE2BT2+82
kStr4:				EQU kE2BT2+83
kStr5:				EQU kE2BT2+84
kStr6:				EQU kE2BT2+85
kStr7:				EQU kE2BT2+86
kStr8:				EQU kE2BT2+87
kStr9:				EQU kE2BT2+88
kStr0:				EQU kE2BT2+89
kFinN:				EQU kE2BT2+90
kFinI:				EQU kE2BT2+91
kFinPV:				EQU kE2BT2+92
kFinPMT:				EQU kE2BT2+93
kFinFV:				EQU kE2BT2+94
kFinPY:				EQU kE2BT2+95
kFinCY:				EQU kE2BT2+96
kFinFPMT:				EQU kE2BT2+97
kFinFI:				EQU kE2BT2+98
kFinFPV:				EQU kE2BT2+99
kFinFN:				EQU kE2BT2+100
kFinFFV:				EQU kE2BT2+101
kFinNPV:				EQU kE2BT2+102
kFinIRR:				EQU kE2BT2+103
kFinBAL:				EQU kE2BT2+104
kFinPRN:				EQU kE2BT2+105
kFinINT:				EQU kE2BT2+106
kSumX:				EQU kE2BT2+107
kSumX2:				EQU kE2BT2+108
kFinToNom:				EQU kE2BT2+109
kFinToEff:				EQU kE2BT2+110
kFinDBD:				EQU kE2BT2+111
kStatVP:				EQU kE2BT2+112
kStatZ:				EQU kE2BT2+113
kStatT:				EQU kE2BT2+114
kStatChi:				EQU kE2BT2+115
kStatF:				EQU kE2BT2+116
kStatDF:				EQU kE2BT2+117
kStatPhat:				EQU kE2BT2+118
kStatPhat1:				EQU kE2BT2+119
kStatPhat2:				EQU kE2BT2+120
kStatMeanX1:				EQU kE2BT2+121
kStatMeanX2:				EQU kE2BT2+122
kStatStdX1:				EQU kE2BT2+123
kStatStdX2:				EQU kE2BT2+124
kStatStdXP:				EQU kE2BT2+125
kStatN1:				EQU kE2BT2+126
kStatN2:				EQU kE2BT2+127
kStatLower:				EQU kE2BT2+128
kStatUpper:				EQU kE2BT2+129
kuw0:				EQU kE2BT2+130
kImag:				EQU kE2BT2+131
kSumY:				EQU kE2BT2+132
kXres:				EQU kE2BT2+133
kStat_s:				EQU kE2BT2+134
kSumY2:				EQU kE2BT2+135
kSumXY:				EQU kE2BT2+136
kuXres:				EQU kE2BT2+137
kModBox:				EQU kE2BT2+138
kNormProb:				EQU kE2BT2+139
kNormalPDF:				EQU kE2BT2+140
kTPDF:				EQU kE2BT2+141
kChiPDF:				EQU kE2BT2+142
kFPDF:				EQU kE2BT2+143
kMinY:				EQU kE2BT2+144 ;MinY
kRandBin:				EQU kE2BT2+145
kRef:				EQU kE2BT2+146
kRRef:				EQU kE2BT2+147
kLRSqr:				EQU kE2BT2+148
kBRSqr:				EQU kE2BT2+149
kDiagOn:				EQU kE2BT2+150
kDiagOff:				EQU kE2BT2+151
kun1:				EQU kE2BT2+152 ;FOR RCL USE WHEN GOTTEN FROM 82
kvn1:				EQU kE2BT2+153 ;FOR RCL USE WHEN GOTTEN FROM 82
k83_00End:				EQU kvn1 ;end of original keys...
kArchive:				EQU k83_00End + 1
kUnarchive:				EQU k83_00End + 2
kAsm:				EQU k83_00End + 3 ;Asm(
kAsmPrgm:				EQU k83_00End + 4 ;AsmPrgm
kAsmComp:				EQU k83_00End + 5 ;AsmComp(
kcapAAcute:				EQU k83_00End + 6
kcapAGrave:				EQU k83_00End + 7
kcapACaret:				EQU k83_00End + 8
kcapADier:				EQU k83_00End + 9
kaAcute:				EQU k83_00End + 10
kaGrave:				EQU k83_00End + 11
kaCaret:				EQU k83_00End + 12
kaDier:				EQU k83_00End + 13
kcapEAcute:				EQU k83_00End + 14
kcapEGrave:				EQU k83_00End + 15
kcapECaret:				EQU k83_00End + 16
kcapEDier:				EQU k83_00End + 17
keAcute:				EQU k83_00End + 18
keGrave:				EQU k83_00End + 19
keCaret:				EQU k83_00End + 20
keDier:				EQU k83_00End + 21
kcapIAcute:				EQU k83_00End + 22
kcapIGrave:				EQU k83_00End + 23
kcapICaret:				EQU k83_00End + 24
kcapIDier:				EQU k83_00End + 25
kiAcute:				EQU k83_00End + 26
kiGrave:				EQU k83_00End + 27
kiCaret:				EQU k83_00End + 28
kiDier:				EQU k83_00End + 29
kcapOAcute:				EQU k83_00End + 30
kcapOGrave:				EQU k83_00End + 31
kcapOCaret:				EQU k83_00End + 32
kcapODier:				EQU k83_00End + 33
koAcute:				EQU k83_00End + 34
koGrave:				EQU k83_00End + 35
koCaret:				EQU k83_00End + 36
koDier:				EQU k83_00End + 37
kcapUAcute:				EQU k83_00End + 38
kcapUGrave:				EQU k83_00End + 39
kcapUCaret:				EQU k83_00End + 40
kcapUDier:				EQU k83_00End + 41
kuAcute:				EQU k83_00End + 42
kuGrave:				EQU k83_00End + 43
kuCaret:				EQU k83_00End + 44
kuDier:				EQU k83_00End + 45
kcapCCed:				EQU k83_00End + 46
kcCed:				EQU k83_00End + 47
kcapNTilde:				EQU k83_00End + 48
knTilde:				EQU k83_00End + 49
kaccent:				EQU k83_00End + 50
kgrave:				EQU k83_00End + 51
kdieresis:				EQU k83_00End + 52
kquesDown:				EQU k83_00End + 53
kexclamDown:				EQU k83_00End + 54
kalpha:				EQU k83_00End + 55
kbeta:				EQU k83_00End + 56
kgamma:				EQU k83_00End + 57
kcapDelta:				EQU k83_00End + 58
kdelta:				EQU k83_00End + 59
kepsilon:				EQU k83_00End + 60
klambda:				EQU k83_00End + 61
kmu:				EQU k83_00End + 62
kpi2:				EQU k83_00End + 63
krho:				EQU k83_00End + 64
kcapSigma:				EQU k83_00End + 65
ksigma:				EQU k83_00End + 66
ktau:				EQU k83_00End + 67
kphi:				EQU k83_00End + 68
kcapOmega:				EQU k83_00End + 69
kphat:				EQU k83_00End + 70
kchi2:				EQU k83_00End + 71
kstatF2:				EQU k83_00End + 72
kLa:				EQU k83_00End + 73
kLb:				EQU k83_00End + 74
kLc:				EQU k83_00End + 75
kLd:				EQU k83_00End + 76
kLe:				EQU k83_00End + 77
kLf:				EQU k83_00End + 78
kLg:				EQU k83_00End + 79
kLh:				EQU k83_00End + 80
kLi:				EQU k83_00End + 81
kLj:				EQU k83_00End + 82
kLk:				EQU k83_00End + 83
kLl:				EQU k83_00End + 84
kLm:				EQU k83_00End + 85
kLsmalln:				EQU k83_00End + 86
kLo:				EQU k83_00End + 87
kLp:				EQU k83_00End + 88
kLq:				EQU k83_00End + 89
kLsmallr:				EQU k83_00End + 90
kLs:				EQU k83_00End + 91
kLt:				EQU k83_00End + 92
kLu:				EQU k83_00End + 93
kLv:				EQU k83_00End + 94
kLw:				EQU k83_00End + 95
kLx:				EQU k83_00End + 96
kLy:				EQU k83_00End + 97
kLz:				EQU k83_00End + 98
kGarbageC:				EQU k83_00End + 99 ;GarbageCollect
kE2BT2_End:				EQU kGarbageC

;TI-83 Plus Context Equates
;---------------------------------------------
cxCmd:				EQU kQuit ;home screen
cxMatEdit:				EQU kMatrixEd ;matrix editor
cxPrgmEdit:				EQU kPrgmEd ;program editor
cxEquEdit:				EQU kYequ ;equation editor
cxGrRange:				EQU kWindow ;graph range editor
cxGrZfact:				EQU kZFactEd ;graph zoom factors editor
cxGraph:				EQU kGraph ;graph mode
cxStatEdit:				EQU kStatEd ;statistics list editor
cxPrgmInput:				EQU kPrgmInput ;programmed input
cxError:				EQU kError ;error handler
cxLinkIO:				EQU kLinkIO ;LINK I/O interface
cxMem:				EQU kResetMem ;reset memory
cxDefMem:				EQU kResetDef ;reset default
cxRAMApp:				EQU kChkRAM ;RAM usage screen
cxMode:				EQU kMode ;mode settings screen
cxErase:				EQU kDelMem ;memory erase
cxPrgmCreate:				EQU kPrgmCr ;PROGRAM CREATE
cxTableEditor:				EQU kTable ;TABLE EDITOR
cxTableSet:				EQU kTblSet ;TABLE SET UP
cxStatPlot:				EQU kStatP ;STAT PLOTS
cxInfStat:				EQU kInfStat ;Inferential Statistic
cxFormat:				EQU kFormat ;FORMAT CONTEXT
cxSolveTVM:				EQU kSolveTVM ;Solve TVM
cxSolveRoot:				EQU kSolveRoot ;Solve Root
lastOldApp:				EQU kExtApps ;external applications
cxextapps:				EQU kExtApps
cxNewApps:				EQU kNewApps ;new cerberus applications
cxGroup:				EQU cxNewApps+0 ;1st new app.
cxUnGroup:				EQU cxNewApps+1 ;2nd new app.
lastNewApp:				EQU cxUnGroup ;last new app for this ver

;Scan Code Equates
;-------------------------------
skDown:				EQU 01h
skLeft:				EQU 02h
skRight:				EQU 03h
skUp:				EQU 04h
skEnter:				EQU 09h
skAdd:				EQU 0Ah
skSub:				EQU 0Bh
skMul:				EQU 0Ch
skDiv:				EQU 0Dh
skPower:				EQU 0Eh
skClear:				EQU 0Fh
skChs:				EQU 11h
sk3:				EQU 12h
sk6:				EQU 13h
sk9:				EQU 14h
skRParen:				EQU 15h
skTan:				EQU 16h
skVars:				EQU 17h
skDecPnt:				EQU 19h
sk2:				EQU 1Ah
sk5:				EQU 1Bh
sk8:				EQU 1Ch
skLParen:				EQU 1Dh
skCos:				EQU 1Eh
skPrgm:				EQU 1Fh
skStat:				EQU 20h
sk0:				EQU 21h
sk1:				EQU 22h
sk4:				EQU 23h
sk7:				EQU 24h
skComma:				EQU 25h
skSin:				EQU 26h
skMatrix:				EQU 27h
skGraphvar:				EQU 28h
skStore:				EQU 2Ah
skLn:				EQU 2Bh
skLog:				EQU 2Ch
skSquare:				EQU 2Dh
skRecip:				EQU 2Eh
skMath:				EQU 2Fh
skAlpha:				EQU 30h
skGraph:				EQU 31h
skTrace:				EQU 32h
skZoom:				EQU 33h
skWindow:				EQU 34h
skYEqu:				EQU 35h
sk2nd:				EQU 36h
skMode:				EQU 37h
skDel:				EQU 38h

;Tokens
;----------------------------------------------------
EOSSTART:				EQU 0

;DISPLAY CONVERSIONS COME IMMEDIATELY BEFORE 'TSTORE'
;
DCONV:				EQU 01h
;
tToDMS:				EQU DCONV ;01h
tToDEC:				EQU DCONV+1 ;02h
tToAbc:				EQU DCONV+2 ;03h > A b/c
;
tStore:				EQU DCONV+3 ;04h Lstore 01
;
tBoxPlot:				EQU 05h
;
BRACKS:				EQU 06h
;
tLBrack:				EQU BRACKS ;06h '['
tRBrack:				EQU BRACKS+1 ;07h ']'
tLBrace:				EQU BRACKS+2 ;08h '{'
tRBrace:				EQU BRACKS+3 ;09h '}'
;
tPOST1:				EQU BRACKS+4
;
tFromRad:				EQU tPOST1 ;0Ah Lradian
tFromDeg:				EQU tPOST1+1 ;0Bh Ldegree
tRecip:				EQU tPOST1+2 ;0Ch Linverse
tSqr:				EQU tPOST1+3 ;0Dh Lsquare
tTrnspos:				EQU tPOST1+4 ;0Eh Ltranspose
tCube:				EQU tPOST1+5 ;0Fh '^3'
;
tLParen:				EQU 10h ;10h '('
tRParen:				EQU 11h ;11h ')'
;
IMUN:				EQU 12h
;
tRound:				EQU IMUN ;12h 'round'
tPxTst:				EQU IMUN+1 ;13h 'PXL-TEST'
tAug:				EQU IMUN+2 ;14h 'aug'
tRowSwap:				EQU IMUN+3 ;15h 'rSwap'
tRowPlus:				EQU IMUN+4 ;16h 'rAdd'
tmRow:				EQU IMUN+5 ;17h 'multR'
tmRowPlus:				EQU IMUN+6 ;18h 'mRAdd'
tMax:				EQU IMUN+7 ;19h 'max'
tMin:				EQU IMUN+8 ;1Ah 'min'
tRToPr:				EQU IMUN+9 ;1Bh 'R>Pr
tRToPo:				EQU IMUN+10 ;1Ch 'R>Po
tPToRx:				EQU IMUN+11 ;1Dh 'P>Rx
tPToRy:				EQU IMUN+12 ;1Eh 'P>Ry
tMedian:				EQU IMUN+13 ;1Fh 'MEDIAN
tRandM:				EQU IMUN+14 ;20h 'randM'
tMean:				EQU IMUN+15 ;21h
tRoot:				EQU IMUN+16 ;22h 'ROOT'
tSeries:				EQU IMUN+17 ;23h 'seq'
tFnInt:				EQU IMUN+18 ;24h 'fnInt'
tNDeriv:				EQU IMUN+19 ;25h 'fnIr'
tEvalF:				EQU IMUN+20 ;26h
tFmin:				EQU IMUN+21 ;27h
tFmax:				EQU IMUN+22 ;28h
;
tEOSEL:				EQU IMUN+23
;
tSpace:				EQU tEOSEL ;29h ' '
tString:				EQU tEOSEL+1 ;2Ah '"'
tComma:				EQU tEOSEL+2 ;2Bh ','
;
tii:				EQU 2Ch ;i

;Postfix Functions
;-----------------------------------------
tPost:				EQU 2Dh
;
tFact:				EQU tPost ;2Dh '!'
;
tCubicR:				EQU 2Eh
tQuartR:				EQU 2Fh

;Number Tokens 
;---------------------------------------
NUMS:				EQU 30h
;
t0:				EQU NUMS ;30h
t1:				EQU NUMS+1 ;31h
t2:				EQU NUMS+2 ;32h
t3:				EQU NUMS+3 ;33h
t4:				EQU NUMS+4 ;34h
t5:				EQU NUMS+5 ;35h
t6:				EQU NUMS+6 ;36h
t7:				EQU NUMS+7 ;37h
t8:				EQU NUMS+8 ;38h
t9:				EQU NUMS+9 ;39h
tDecPt:				EQU NUMS+10 ;3Ah
tee:				EQU NUMS+11 ;3Bh

;Binary OP
;-------------------------------------------
tOr:				EQU 3Ch ;3Ch '_or_'
tXor:				EQU 3Dh ;3Dh
;
tColon:				EQU 3Eh ;3Eh ':'
tEnter:				EQU 3Fh ;3Fh Lenter
;
tAnd:				EQU 40h ;40h '_and_'

;Letter Tokens
;--------------------------------------
LET:				EQU 41h
tA:				EQU LET ;41h
tB:				EQU LET+1 ;42h
tC:				EQU LET+2 ;43h
tD:				EQU LET+3 ;44h
tE:				EQU LET+4 ;45h
tF:				EQU LET+5 ;46h
tG:				EQU LET+6 ;47h
tH:				EQU LET+7 ;48h
tI:				EQU LET+8 ;49h
tJ:				EQU LET+9 ;4Ah
tK:				EQU LET+10 ;4Bh
tL:				EQU LET+11 ;4Ch
tM:				EQU LET+12 ;4Dh
tN:				EQU LET+13 ;4Eh
tO:				EQU LET+14 ;4Fh
tP:				EQU LET+15 ;50h
tQ:				EQU LET+16 ;51h
tR:				EQU LET+17 ;52h
tS:				EQU LET+18 ;53h
tT:				EQU LET+19 ;54h
tU:				EQU LET+20 ;55h
tV:				EQU LET+21 ;56h
tW:				EQU LET+22 ;57h
tX:				EQU LET+23 ;58h
tY:				EQU LET+24 ;59h
tZ:				EQU LET+25 ;5Ah
tTheta:				EQU LET+26 ;5Bh

;These Var Tokens Are 1st Of A Double Token
;------------------------------------------

vToks:				EQU LET+27

;User Matrix Token, 2nd Token Needed For Name
;
tVarMat:				EQU vToks ;5Ch
;
;User List Token, 2nd Token Needed For Name
;
tVarLst:				EQU vToks+1 ;5Dh
;
;User Equation Token, 2nd Token Needed For Name
;
tVarEqu:				EQU vToks+2 ;5Eh
tProg:				EQU vToks+3 ;5Fh
;
;User Pict Token, 2nd Token Needed For Name
;
tVarPict:				EQU vToks+4 ;60h
;
;User GDB Token, 2nd Token Needed For Name
;
tVarGDB:				EQU vToks+5 ;61h
tVarOut:				EQU vToks+6 ;62h
tVarSys:				EQU vToks+7 ;63h

;Mode Setting Commands
;-------------------------------------------------
MODESA:				EQU vToks+8 ;64h
tRad:				EQU MODESA ;64h 'Radian'
tDeg:				EQU MODESA+1 ;65h 'Degree'
tNormF:				EQU MODESA+2 ;66h 'Normal'
tSci:				EQU MODESA+3 ;67h 'Sci'
tEng:				EQU MODESA+4 ;68h 'Eng'
tFloat:				EQU MODESA+5 ;69h 'Float'
CMPS:				EQU 6Ah
tEQ:				EQU CMPS ;6Ah '=='
tLT:				EQU CMPS+1 ;6Bh '<'
tGT:				EQU CMPS+2 ;6Ch '>'
tLE:				EQU CMPS+3 ;6Dh LLE
tGE:				EQU CMPS+4 ;6Eh LGE
tNE:				EQU CMPS+5 ;6Fh LNE

;Binary OP
;---------------------------------------
tAdd:				EQU 70h ;70h '+'
tSub:				EQU 71h ;71h '-'
tAns:				EQU 72h ;72h

;Mode Setting Commands
;-------------------------------------------------------
MODES:				EQU 73h
tFix:				EQU MODES ;73h 'Fix_'
tSplitOn:				EQU MODES+1 ;74h
tFullScreen:				EQU MODES+2 ;75h
tStndrd:				EQU MODES+3 ;76h 'Func'
tParam:				EQU MODES+4 ;77h 'Param'
tPolar:				EQU MODES+5 ;78h 'Pol'
tSeqG:				EQU MODES+6 ;79h
tAFillOn:				EQU MODES+7 ;7Ah 'AUTO FILL ON'
tAFillOff:				EQU MODES+8 ;7Bh
tACalcOn:				EQU MODES+9 ;7Ch
tACalcOff:				EQU MODES+10 ;7Dh 'AutoFill OFF'

;Graph Format Tokens Are 2 Byte Tokens
;----------------------------------------
tGFormat:				EQU MODES+11 ;7Eh
tBoxIcon:				EQU 7Fh
tCrossIcon:				EQU 80h
tDotIcon:				EQU 81h

;(More) Binary OP
;---------------------------------------
tMul:				EQU 82h ;82h '*'
tDiv:				EQU 83h ;83h '/'

;Some Graph Commands
;------------------------------------------------------
GRCMDS:				EQU 84h
tTrace:				EQU GRCMDS ; 84h 'Trace'
tClDrw:				EQU GRCMDS+1 ; 85h 'ClDrw'
tZoomStd:				EQU GRCMDS+2 ; 86h 'ZStd'
tZoomtrg:				EQU GRCMDS+3 ; 87h 'Ztrg'
tZoomBox:				EQU GRCMDS+4 ; 88h 'ZBOX'
tZoomIn:				EQU GRCMDS+5 ; 89h 'ZIn'
tZoomOut:				EQU GRCMDS+6 ; 8Ah 'ZOut'
tZoomSqr:				EQU GRCMDS+7 ; 8Bh 'ZSqr'
tZoomInt:				EQU GRCMDS+8 ; 8Ch 'ZInt'
tZoomPrev:				EQU GRCMDS+9 ; 8Dh 'ZPrev'
tZoomDec:				EQU GRCMDS+10 ; 8Eh 'ZDecm'
tZoomStat:				EQU GRCMDS+11 ; 8Fh 'ZStat
tUsrZm:				EQU GRCMDS+12 ; 90h 'ZRcl'
tPrtScrn:				EQU GRCMDS+13 ; 91h 'PrtScrn'
tZoomSto:				EQU GRCMDS+14 ; 92h  ZOOM STORE
tText:				EQU GRCMDS+15 ; 93h

;Binary OP (Combination & Permutation)
;-------------------------------------------------
tnPr:				EQU GRCMDS+16 ;94h '_nPr_'
tnCr:				EQU GRCMDS+17 ;95h '_nCr_'

;More Graph Commands
;--------------------------------------------------
tYOn:				EQU GRCMDS+18 ;96h 'FnOn_'
tYOff:				EQU GRCMDS+19 ;97h 'FnOff_'
tStPic:				EQU GRCMDS+20 ;98h 'StPic_'
tRcPic:				EQU GRCMDS+21 ;99h 'RcPic_'
tStoDB:				EQU GRCMDS+22 ;9Ah 'StGDB_'
tRclDB:				EQU GRCMDS+23 ;9Bh 'RcGDB_'
tLine:				EQU GRCMDS+24 ;9Ch 'Line'
tVert:				EQU GRCMDS+25 ;9Dh 'Vert_'
tPtOn:				EQU GRCMDS+26 ;9Eh 'PtOn'
tPtOff:				EQU GRCMDS+27 ;9Fh 'PtOff'

;Token A0 Cannot Be An EOS Function Since Low MULT=A0 Already
;------------------------------------------------------------
tPtChg:				EQU GRCMDS+28 ;A0h 'PtChg'
tPXOn:				EQU GRCMDS+29 ;A1h
tPXOff:				EQU GRCMDS+30 ;A2h
tPXChg:				EQU GRCMDS+31 ;A3h
tShade:				EQU GRCMDS+32 ;A4h 'Shade'
tCircl:				EQU GRCMDS+33 ;A5h 'Circl'
tHorz:				EQU GRCMDS+34 ;A6h 'HORIZONTAL'
tTanLn:				EQU GRCMDS+35 ;A7h 'TanLn'
tDrInv:				EQU GRCMDS+36 ;A8h 'DrInv_'
tDrawF:				EQU GRCMDS+37 ;A9h 'DrawF_'
tVarStrng:				EQU 0AAh

;Functions with No Arguments                                    
;--------------------------------------------------
NOARG:				EQU 0ABh
tRand:				EQU NOARG ;ABh 'rand'
tPi:				EQU NOARG+1 ;ACh  Lpi
tGetKey:				EQU NOARG+2 ;ADh 'getKy'
tAPost:				EQU tGetKey+1 ;APOSTROPHY
tQuest:				EQU tAPost+1 ;QUESTION MARK
UNARY:				EQU tQuest+1 ;B0h
tChs:				EQU UNARY ;B0h
tInt:				EQU UNARY+1 ;B1h
tAbs:				EQU UNARY+2 ;B2h
tDet:				EQU UNARY+3 ;B3h
tIdent:				EQU UNARY+4 ;B4h
tDim:				EQU UNARY+5 ;B5h
tSum:				EQU UNARY+6 ;B6h
tProd:				EQU UNARY+7 ;B7h
tNot:				EQU UNARY+8 ;B8h
tIPart:				EQU UNARY+9 ;B9h
tFPart:				EQU UNARY+10 ;BAh

;New 2 Byte Tokens
;------------------------------------------
t2ByteTok:				EQU 0BBh
UNARYLR:				EQU UNARY+12
tSqrt:				EQU UNARYLR ;BCh
tCubRt:				EQU UNARYLR+1 ;BDh
tLn:				EQU UNARYLR+2 ;BEh
tExp:				EQU UNARYLR+3 ;BFh
tLog:				EQU UNARYLR+4 ;C0h
tALog:				EQU UNARYLR+5 ;C1h
tSin:				EQU UNARYLR+6 ;C2h
tASin:				EQU UNARYLR+7 ;C3h
tCos:				EQU UNARYLR+8 ;C4h
tACos:				EQU UNARYLR+9 ;C5h
tTan:				EQU UNARYLR+10 ;C6h
tATan:				EQU UNARYLR+11 ;C7h
tSinH:				EQU UNARYLR+12 ;C8h
tASinH:				EQU UNARYLR+13 ;C9h
tCoshH:				EQU UNARYLR+14 ;CAh
tACosH:				EQU UNARYLR+15 ;CBh
tTanH:				EQU UNARYLR+16 ;CCh
tATanH:				EQU UNARYLR+17 ;CDh

;Some Programming Commands
;------------------------------------------------------
PROGTOK:				EQU UNARYLR+18
tIf:				EQU PROGTOK ;CEh
tThen:				EQU PROGTOK+1 ;CFh
tElse:				EQU PROGTOK+2 ;D0h
tWhile:				EQU PROGTOK+3 ;D1h
tRepeat:				EQU PROGTOK+4 ;D2h
tFor:				EQU PROGTOK+5 ;D3h
tEnd:				EQU PROGTOK+6 ;D4h
tReturn:				EQU PROGTOK+7 ;D5h
tLbl:				EQU PROGTOK+8 ;D6h 'Lbl_'
tGoto:				EQU PROGTOK+9 ;D7h 'Goto_'
tPause:				EQU PROGTOK+10 ;D8h 'Pause_'
tStop:				EQU PROGTOK+11 ;D9h 'Stop'
tISG:				EQU PROGTOK+12 ;DAh 'IS>'
tDSL:				EQU PROGTOK+13 ;DBh 'DS<'
tInput:				EQU PROGTOK+14 ;DCh 'Input_'
tPrompt:				EQU PROGTOK+15 ;DDh 'Prompt_'
tDisp:				EQU PROGTOK+16 ;DEh 'Disp_'
tDispG:				EQU PROGTOK+17 ;DFh 'DispG'
tOutput:				EQU PROGTOK+18 ;E0h 'Outpt'
tClLCD:				EQU PROGTOK+19 ;E1h 'ClLCD'
tConst:				EQU PROGTOK+20 ;E2h 'Fill'
tSortA:				EQU PROGTOK+21 ;E3h 'sortA_'
tSortD:				EQU PROGTOK+22 ;E4h 'sortD_'
tDispTab:				EQU PROGTOK+23 ;E5h 'Disp Table
tMenu:				EQU PROGTOK+24 ;E6h 'Menu'
tSendMBL:				EQU PROGTOK+25 ;E7h 'SEND'
tGetMBL:				EQU PROGTOK+26 ;E8h 'GET'

;Stat Plot Commands
;------------------------------------------------------
statPCmd:				EQU PROGTOK+27
tPlotOn:				EQU statPCmd ;E9h ' PLOTSON'
tPlotOff:				EQU statPCmd+1 ;EAh ' PLOTSOFF
tListName:				EQU 0EBh ;LIST DESIGNATOR
tPlot1:				EQU 0ECh
tPlot2:				EQU 0EDh
tPlot3:				EQU 0EEh
tUnused01:				EQU 0EFh ;available?
tPower:				EQU 0F0h ;'^'
tXRoot:				EQU 0F1h ;LsupX,Lroot
STATCMD:				EQU 0F2h
tOneVar:				EQU STATCMD ;F2h 'OneVar_'
tTwoVar:				EQU STATCMD+1 ;F3h
tLR:				EQU STATCMD+2 ;F4h 'LinR(A+BX
tLRExp:				EQU STATCMD+3 ;F5h 'ExpR_'
tLRLn:				EQU STATCMD+4 ;F6h 'LnR_'
tLRPwr:				EQU STATCMD+5 ;F7h 'PwrR_'
tMedMed:				EQU STATCMD+6 ;F8h
tQuad:				EQU STATCMD+7 ;F9h
tClrLst:				EQU STATCMD+8 ;FAh 'CLEAR LIST
tClrTbl:				EQU STATCMD+9 ;FBh CLEAR TABLE
tHist:				EQU STATCMD+10 ;FCh 'Hist_'
txyLine:				EQU STATCMD+11 ;FDh 'xyline_'
tScatter:				EQU STATCMD+12 ;FEh 'Scatter_'
tLR1:				EQU STATCMD+13 ;FFh 'LINR(AX+B

;2nd Half Of Graph Format Tokens
;----------------------------------------------
;           Format settings commands
;
GFMT:				EQU 0
tSeq:				EQU GFMT ; 'SeqG'
tSimulG:				EQU GFMT+1 ; 'SimulG'
tPolarG:				EQU GFMT+2 ; 'PolarGC'
tRectG:				EQU GFMT+3 ; 'RectGC'
tCoordOn:				EQU GFMT+4 ; 'CoordOn'
tCoordOff:				EQU GFMT+5 ; 'CoordOff'
tDrawLine:				EQU GFMT+6 ; 'DrawLine'
tDrawDot:				EQU GFMT+7 ; 'DrawDot'
tAxisOn:				EQU GFMT+8 ; 'AxesOn'
tAxisOff:				EQU GFMT+9 ; 'AxesOff'
tGridOn:				EQU GFMT+10 ; 'GridOn'
tGridOff:				EQU GFMT+11 ; 'GridOff'
tLblOn:				EQU GFMT+12 ; 'LabelOn'
tLblOff:				EQU GFMT+13 ; 'LabelOff'
tWebOn:				EQU GFMT+14 ; 'WebOn'
tWebOff:				EQU GFMT+15 ; 'WebOFF'
tuv:				EQU GFMT+16 ; U vs V
tvw:				EQU GFMT+17 ; V vs W
tuw:				EQU GFMT+18 ; U vs W

;2nd Half Of User Matrix Tokens
;-------------------------------------
tMatA:				EQU 00h ;MAT A
tMatB:				EQU 01h ;MAT B
tMatC:				EQU 02h ;MAT C
tMatD:				EQU 03h ;MAT D
tMatE:				EQU 04h ;MAT E
tMatF:				EQU 05h ;MAT F
tMatG:				EQU 06h ;MAT G
tMatH:				EQU 07h ;MAT H
tMatI:				EQU 08h ;MAT I
tMatJ:				EQU 09h ;MAT J

;2nd Half Of User List Tokens
;--------------------------------------
tL1:				EQU 00h ;LIST 1
tL2:				EQU 01h ;LIST 2
tL3:				EQU 02h ;LIST 3
tL4:				EQU 03h ;LIST 4
tL5:				EQU 04h ;LIST 5
tL6:				EQU 05h ;LIST 6

;2nd Half Of User Equation Tokens
;----------------------------------
;  "Y" EQUATIONS HAVE BIT 4 SET
;
tY1:				EQU 10h ;Y1
tY2:				EQU 11h ;Y2
tY3:				EQU 12h ;Y3
tY4:				EQU 13h ;Y4
tY5:				EQU 14h ;Y5
tY6:				EQU 15h ;Y6
tY7:				EQU 16h ;Y7
tY8:				EQU 17h ;Y8
tY9:				EQU 18h ;Y9
tY0:				EQU 19h ;Y0

;Param Equations Have Bit 5 Set
;-----------------------------------
tX1T:				EQU 20h ;X1t
tY1T:				EQU 21h ;Y1t
tX2T:				EQU 22h ;X2t
tY2T:				EQU 23h ;Y2t
tX3T:				EQU 24h ;X3t
tY3T:				EQU 25h ;Y3t
tX4T:				EQU 26h ;X4t
tY4T:				EQU 27h ;Y4t
tX5T:				EQU 28h ;X5t
tY5T:				EQU 29h ;Y5t
tX6T:				EQU 2Ah ;X6t
tY6T:				EQU 2Bh ;Y6t

;Polar Equations Have Bit 6 Set
;----------------------------------
tR1:				EQU 40h ;R1
tR2:				EQU 41h ;R2
tR3:				EQU 42h ;R3
tR4:				EQU 43h ;R4
tR5:				EQU 44h ;R5
tR6:				EQU 45h ;R6

;Recursion Equations Have Bit 7 Set
;----------------------------------
tun:				EQU 80h ;Un
tvn:				EQU 81h ;Vn
twn:				EQU 82h ;Wn

;2nd Half Of User Picture Tokens
;------------------------------------
tPic1:				EQU 00h ;PIC1
tPic2:				EQU 01h ;PIC2
tPic3:				EQU 02h ;PIC3
tPic4:				EQU 03h ;PIC4
tPic5:				EQU 04h ;PIC5
tPic6:				EQU 05h ;PIC6
tPic7:				EQU 06h ;PIC7
tPic8:				EQU 07h ;PIC8
tPic9:				EQU 08h ;PIC9
tPic0:				EQU 09h ;PIC0

;2nd Half Of User Graph Database Tokens
;--------------------------------------
tGDB1:				EQU 00h ;GDB1
tGDB2:				EQU 01h ;GDB2
tGDB3:				EQU 02h ;GDB3
tGDB4:				EQU 03h ;GDB4
tGDB5:				EQU 04h ;GDB5
tGDB6:				EQU 05h ;GDB6
tGDB7:				EQU 06h ;GDB7
tGDB8:				EQU 07h ;GDB8
tGDB9:				EQU 08h ;GDB9
tGDB0:				EQU 09h ;GDB0

;2nd Half Of String Vars
;------------------------------
tStr1:				EQU 00h
tStr2:				EQU 01h
tStr3:				EQU 02h
tStr4:				EQU 03h
tStr5:				EQU 04h
tStr6:				EQU 05h
tStr7:				EQU 06h
tStr8:				EQU 07h
tStr9:				EQU 08h
tStr0:				EQU 09h

;2nd Half Of System Output Only Variables
;-----------------------------------------------------------------
;OPEN			equ 00h
tRegEq:				EQU 01h ;REGRESSION EQUATION
tStatN:				EQU 02h ;STATISTICS N
tXMean:				EQU 03h ;X MEAN
tSumX:				EQU 04h ;SUM(X)
tSumXSqr:				EQU 05h ;SUM(X^2)
tStdX:				EQU 06h ;STANDARD DEV X
tStdPX:				EQU 07h ;STANDARD DEV POP X
tMinX:				EQU 08h ;Min X VALUE
tMaxX:				EQU 09h ;Max X VALUE
tMinY:				EQU 0Ah ;Min Y VALUE
tMaxY:				EQU 0Bh ;Max Y VALUE
tYmean:				EQU 0Ch ;Y MEAN
tSumY:				EQU 0Dh ;SUM(Y)
tSumYSqr:				EQU 0Eh ;SUM(Y^2)
tStdY:				EQU 0Fh ;STANDARD DEV Y
tStdPY:				EQU 10h ;STANDARD DEV POP Y
tSumXY:				EQU 11h ;SUM(XY)
tCorr:				EQU 12h ;CORRELATION
tMedX:				EQU 13h ;MED(X)
tQ1:				EQU 14h ;1ST QUADRANT OF X
tQ3:				EQU 15h ;3RD QUADRANT OF X
tQuadA:				EQU 16h ;1ST TERM OF QUAD POLY REG/ Y-INT
tQuadB:				EQU 17h ;2ND TERM OF QUAD POLY REG/ SLOPE
tQuadC:				EQU 18h ;3RD TERM OF QUAD POLY REG
tCubeD:				EQU 19h ;4TH TERM OF CUBIC POLY REG
tQuartE:				EQU 1Ah ;5TH TERM OF QUART POLY REG
tMedX1:				EQU 1Bh ;x1 FOR MED-MED
tMedX2:				EQU 1Ch ;x2 FOR MED-MED
tMedX3:				EQU 1Dh ;x3 FOR MED-MED
tMedY1:				EQU 1Eh ;y1 FOR MED-MED
tMedY2:				EQU 1Fh ;y2 FOR MED-MED
tMedY3:				EQU 20h ;y3 FOR MED-MED
tRecurn:				EQU 21h	;RECURSION N
tStatP:				EQU 22h
tStatZ:				EQU 23h
tStatT:				EQU 24h
tStatChi:				EQU 25h
tStatF:				EQU 26h
tStatDF:				EQU 27h
tStatPhat:				EQU 28h
tStatPhat1:				EQU 29h
tStatPhat2:				EQU 2Ah
tStatMeanX1:				EQU 2Bh
tStatStdX1:				EQU 2Ch
tStatN1:				EQU 2Dh
tStatMeanX2:				EQU 2Eh
tStatStdX2:				EQU 2Fh
tStatN2:				EQU 30h
tStatStdXP:				EQU 31h
tStatLower:				EQU 32h
tStatUpper:				EQU 33h
tStat_s:				EQU 34h
tLRSqr:				EQU 35h ;r^2
tBRSqr:				EQU 36h ;R^2

;These next tokens are only used to access the data
;they are display only and the user cannot access them at all
;------------------------------------------------------------
tF_DF:				EQU 37h ;ANOFAV FACTOR DF
tF_SS:				EQU 38h ;ANOFAV FACTOR SS
tF_MS:				EQU 39h ;ANOFAV FACTOR MS
tE_DF:				EQU 3Ah ;ANOFAV ERROR DF
tE_SS:				EQU 3Bh ;ANOFAV ERROR SS
tE_MS:				EQU 3Ch ;ANOFAV ERROR MS

;2nd Half Of System Input/Output Variables
;------------------------------------------------
;      SYSTEM VARIABLE EQUATES
;
tuXscl:				EQU 0
tuYscl:				EQU 1
tXscl:				EQU 2
tYscl:				EQU 3
tRecuru0:				EQU 4 ;U 1ST INITIAL COND
tRecurv0:				EQU 5 ;V 1ST INITIAL COND
tun1:				EQU 6 ;U(N-1); NOT USED
tvn1:				EQU 7 ;V(N-1); NOT USED
tuRecuru0:				EQU 8 ;
tuRecurv0:				EQU 9 ;
tXmin:				EQU 0Ah
tXmax:				EQU 0Bh
tYmin:				EQU 0Ch
tYmax:				EQU 0Dh
tTmin:				EQU 0Eh
tTmax:				EQU 0Fh
tThetaMin:				EQU 10h
tThetaMax:				EQU 11h
tuXmin:				EQU 12h
tuXmax:				EQU 13h
tuYmin:				EQU 14h
tuYmax:				EQU 15h
tuThetMin:				EQU 16h
tuThetMax:				EQU 17h
tuTmin:				EQU 18h
tuTmax:				EQU 19h
tTblMin:				EQU 1Ah
tPlotStart:				EQU 1Bh
tuPlotStart:				EQU 1Ch
tnMax:				EQU 1Dh
tunMax:				EQU 1Eh
tnMin:				EQU 1Fh
tunMin:				EQU 20h
tTblStep:				EQU 21h
tTStep:				EQU 22h
tThetaStep:				EQU 23h
tuTStep:				EQU 24h
tuThetStep:				EQU 25h
tDeltaX:				EQU 26h
tDeltaY:				EQU 27h
tXFact:				EQU 28h
tYFact:				EQU 29h
tTblInput:				EQU 2Ah
tFinN:				EQU 2Bh
tFinI:				EQU 2Ch
tFinPV:				EQU 2Dh
tFinPMT:				EQU 2Eh
tFinFV:				EQU 2Fh
tFinPY:				EQU 30h
tFinCY:				EQU 31h
tRecurw0:				EQU 32h ;w0(1)
tuRecurw0:				EQU 33h
tPlotStep:				EQU 34h
tuPlotStep:				EQU 35h
tXres:				EQU 36h
tuXres:				EQU 37h
tRecuru02:				EQU 38h ;u0(2)
tuRecuru02:				EQU 39h
tRecurv02:				EQU 3Ch ;v0(2)
tuRecurv02:				EQU 3Dh
tRecurw02:				EQU 3Eh ;w0(2)
tuRecurw02:				EQU 3Fh

;2nd Byte Of t2ByteTok Tokens
;------------------------------
tFinNPV:				EQU 00h
tFinIRR:				EQU 01h
tFinBAL:				EQU 02h
tFinPRN:				EQU 03h
tFinINT:				EQU 04h
tFinToNom:				EQU 05h
tFinToEff:				EQU 06h
tFinDBD:				EQU 07h
tLCM:				EQU 08h
tGCD:				EQU 09h
tRandInt:				EQU 0Ah
tRandBin:				EQU 0Bh
tSubStrng:				EQU 0Ch
tStdDev:				EQU 0Dh
tVariance:				EQU 0Eh
tInStrng:				EQU 0Fh
tDNormal:				EQU 10h
tInvNorm:				EQU 11h
tDT:				EQU 12h
tChI:				EQU 13h
tDF:				EQU 14h
tBINPDF:				EQU 15h
tBINCDF:				EQU 16h
tPOIPDF:				EQU 17h
tPOICDF:				EQU 18h
tGEOPDF:				EQU 19h
tGEOCDF:				EQU 1Ah
tNormalPDF:				EQU 1Bh
tTPDF:				EQU 1Ch
tChiPDF:				EQU 1Dh
tFPDF:				EQU 1Eh
tRandNorm:				EQU 1Fh
tFinFPMT:				EQU 20h
tFinFI:				EQU 21h
tFinFPV:				EQU 22h
tFinFN:				EQU 23h
tFinFFV:				EQU 24h
tConj:				EQU 25h
tReal:				EQU 26h
tImag:				EQU 27h
tAngle:				EQU 28h
tCumSum:				EQU 29h
tExpr:				EQU 2Ah
tLength:				EQU 2Bh
tDeltaLst:				EQU 2Ch
tRef:				EQU 2Dh
tRRef:				EQU 2Eh
tToRect:				EQU 2Fh
tToPolar:				EQU 30h
tConste:				EQU 31h
tSinReg:				EQU 32h
tLogistic:				EQU 33h
tLinRegTTest:				EQU 34h
tShadeNorm:				EQU 35h
tShadeT:				EQU 36h
tShadeChi:				EQU 37h
tShadeF:				EQU 38h
tMatToLst:				EQU 39h
tLstToMat:				EQU 3Ah
tZTest:				EQU 3Bh
tTTest:				EQU 3Ch
t2SampZTest:				EQU 3Dh
t1PropZTest:				EQU 3Eh
t2PropZTest:				EQU 3Fh
tChiTest:				EQU 40h
tZIntVal:				EQU 41h
t2SampZInt:				EQU 42h
t1PropZInt:				EQU 43h
t2PropZInt:				EQU 44h
tGraphStyle:				EQU 45h
t2SampTTest:				EQU 46h
t2SampFTest:				EQU 47h
tTIntVal:				EQU 48h
t2SampTInt:				EQU 49h
tSetupLst:				EQU 4Ah
tFinPMTend:				EQU 4Bh
tFinPMTbeg:				EQU 4Ch
tRealM:				EQU 4Dh
tPolarM:				EQU 4Eh
tRectM:				EQU 4Fh
tExprOn:				EQU 50h
tExprOff:				EQU 51h
tClrAllLst:				EQU 52h
tGetCalc:				EQU 53h
tDelVar:				EQU 54h
tEquToStrng:				EQU 55h
tStrngToEqu:				EQU 56h
tDelLast:				EQU 57h
tSelect:				EQU 58h
tANOVA:				EQU 59h
tModBox:				EQU 5Ah
tNormProb:				EQU 5Bh
tMGT:				EQU 64h ;VERTICAL SPLIT
tZFit:				EQU 65h ;ZOOM FIT
tDiag_on:				EQU 66h ;DIANOSTIC DISPLAY ON
tDiag_off:				EQU 67h ;DIANOSTIC DISPLAY OFF
tOkEnd2v0:				EQU 67h ;end of 2byte tokens for version 0.
tArchive:				EQU 68h ;archive
tUnarchive:				EQU 69h ;unarchive
tasm:				EQU 6Ah
tasmComp:				EQU 6Bh	;asm compile
tasmPrgm:				EQU 6Ch	;signifies a program is asm
tasmCmp:				EQU 6Dh	;asm program is compiled
tLcapAAcute:				EQU 6Eh
tLcapAGrave:				EQU 6Fh
tLcapACaret:				EQU 70h
tLcapADier:				EQU 71h
tLaAcute:				EQU 72h
tLaGrave:				EQU 73h
tLaCaret:				EQU 74h
tLaDier:				EQU 75h
tLcapEAcute:				EQU 76h
tLcapEGrave:				EQU 77h
tLcapECaret:				EQU 78h
tLcapEDier:				EQU 79h
tLeAcute:				EQU 7Ah
tLeGrave:				EQU 7Bh
tLeCaret:				EQU 7Ch
tLeDier:				EQU 7Dh
tLcapIGrave:				EQU 7Fh
tLcapICaret:				EQU 80h
tLcapIDier:				EQU 81h
tLiAcute:				EQU 82h
tLiGrave:				EQU 83h
tLiCaret:				EQU 84h
tLiDier:				EQU 85h
tLcapOAcute:				EQU 86h
tLcapOGrave:				EQU 87h
tLcapOCaret:				EQU 88h
tLcapODier:				EQU 89h
tLoAcute:				EQU 8Ah
tLoGrave:				EQU 8Bh
tLoCaret:				EQU 8Ch
tLoDier:				EQU 8Dh
tLcapUAcute:				EQU 8Eh
tLcapUGrave:				EQU 8Fh
tLcapUCaret:				EQU 90h
tLcapUDier:				EQU 91h
tLuAcute:				EQU 92h
tLuGrave:				EQU 93h
tLuCaret:				EQU 94h
tLuDier:				EQU 95h
tLcapCCed:				EQU 96h
tLcCed:				EQU 97h
tLcapNTilde:				EQU 98h
tLnTilde:				EQU 99h
tLaccent:				EQU 9Ah
tLgrave:				EQU 9Bh
tLdieresis:				EQU 9Ch
tLquesDown:				EQU 9Dh
tLexclamDown:				EQU 9Eh
tLalpha:				EQU 9Fh
tLbeta:				EQU 0A0h
tLgamma:				EQU 0A1h
tLcapDelta:				EQU 0A2h
tLdelta:				EQU 0A3h
tLepsilon:				EQU 0A4h
tLlambda:				EQU 0A5h
tLmu:				EQU 0A6h
tLpi:				EQU 0A7h
tLrho:				EQU 0A8h
tLcapSigma:				EQU 0A9h
tLphi:				EQU 0ABh
tLcapOmega:				EQU 0ACh
tLphat:				EQU 0ADh
tLchi:				EQU 0AEh
tLstatF:				EQU 0AFh
tLa:				EQU 0B0h
tLb:				EQU 0B1h
tLc:				EQU 0B2h
tLd:				EQU 0B3h
tLsmalle:				EQU 0B4h
tLf:				EQU 0B5h
tLsmallg:				EQU 0B6h
tLh:				EQU 0B7h
tLi:				EQU 0B8h
tLj:				EQU 0B9h
tLk:				EQU 0BAh
tLl:				EQU 0BCh
tLm:				EQU 0BDh
tLsmalln:				EQU 0BEh
tLo:				EQU 0BFh
tLp:				EQU 0C0h
tLq:				EQU 0C1h
tLsmallr:				EQU 0C2h
tLs:				EQU 0C3h
tLsmallt:				EQU 0C4h
tLu:				EQU 0C5h
tLv:				EQU 0C6h
tLw:				EQU 0C7h
tLx:				EQU 0C8h
tLy:				EQU 0C9h
tLz:				EQU 0CAh
tLsigma:				EQU 0CBh
tLtau:				EQU 0CCh
tLcapIAcute:				EQU 0CDh
tGarbagec:				EQU 0CEh
LastToken:				EQU 0CEh ;tLAST TOKEN IN THIS VERSION...

;Data Type Equates                                                    
;---------------------------------------------------------------------
RealObj:				EQU 0
ListObj:				EQU 1
MatObj:				EQU 2
EquObj:				EQU 3
StrngObj:				EQU 4
ProgObj:				EQU 5
ProtProgObj:				EQU 6
PictObj:				EQU 7
GDBObj:				EQU 8
UnknownObj:				EQU 9
UnknownEquObj:				EQU 0Ah
NewEquObj:				EQU 0Bh
CplxObj:				EQU 0Ch
CListObj:				EQU 0Dh
UndefObj:				EQU 0Eh
WindowObj:				EQU 0Fh
ZStoObj:				EQU 10h
TblRngObj:				EQU 11h
LCDObj:				EQU 12h
BackupObj:				EQU 13h
AppObj:				EQU 14h	;application, only used in menus/link
AppVarObj:				EQU 15h	;application variable
TempProgObj:				EQU 16h ;program, home deletes when finished
GroupObj:				EQU 17h ;group.

;I/O Equates                                                          
;---------------------------------------------------
D0D1_bits:				EQU 03h
D0LD1L:				EQU 03h
D0LD1H:				EQU 01h
D0HD1L:				EQU 02h
D0HD1H:				EQU 00h
bport:				EQU 0 ;4-bit link port (I/O) 

;Device Codes
;-----------------------------------------------------------------
TI82DEV:				EQU 82h
PC82DEV:				EQU 02h
MAC82DEV:				EQU 12h
TI83FDEV:				EQU 73h
LINK83FDEV:				EQU 23h
TI83DEV:				EQU 83h
PC83DEV:				EQU 03h
MAC83DEV:				EQU 13h
TI85DEV:				EQU 95h ;different than real 85 so me talk
PC85DEV:				EQU 05h
MAC85DEV:				EQU 15h
TI73DEV:				EQU 74h ;device x3 is always an 83
PC73DEV:				EQU 07h
MAC73DEV:				EQU 17h
LINK73FDEV:				EQU 23h
PC83FDEV:				EQU 23h

;System Error Codes                                                  
;-----------------------------------------------------------
E_EDITF:				EQU 7 ;allow re-entering application
E_EDIT:				EQU 1<<E_EDITF
E_Mask:				EQU 7Fh
E_Overflow:				EQU 1+E_EDIT
E_DivBy0:				EQU 2+E_EDIT
E_SingularMat:				EQU 3+E_EDIT
E_Domain:				EQU 4+E_EDIT
E_Increment:				EQU 5+E_EDIT
E_Break:				EQU 6+E_EDIT
E_NonReal:				EQU 7+E_EDIT
E_Syntax:				EQU 8+E_EDIT
E_DataType:				EQU 9+E_EDIT
E_Argument:				EQU 10+E_EDIT
E_DimMismatch:				EQU 11+E_EDIT
E_Dimension:				EQU 12+E_EDIT
E_Undefined:				EQU 13+E_EDIT
E_Memory:				EQU 14+E_EDIT
E_Invalid:				EQU 15+E_EDIT
E_IllegalNest:				EQU 16+E_EDIT
E_Bound:				EQU 17+E_EDIT
E_GraphRange:				EQU 18+E_EDIT
E_Zoom:				EQU 19+E_EDIT
E_Label:				EQU 20
E_Stat:				EQU 21
E_Solver:				EQU 22+E_EDIT
E_Singularity:				EQU 23+E_EDIT
E_SignChange:				EQU 24+E_EDIT
E_Iterations:				EQU 25+E_EDIT
E_BadGuess:				EQU 26+E_EDIT
E_StatPlo:				EQU 27
E_TolTooSmall:				EQU 28+E_EDIT
E_Reserved:				EQU 29+E_EDIT
E_Mode:				EQU 30+E_EDIT
E_LnkErr:				EQU 31+E_EDIT
E_LnkMemErr:				EQU 32+E_EDIT
E_LnkTransErr:				EQU 33+E_EDIT
E_LnkDupErr:				EQU 34+E_EDIT
E_LnkMemFull:				EQU 35+E_EDIT
E_Unknown:				EQU 36+E_EDIT
E_Scale:				EQU 37+E_EDIT
E_IdNotFound:				EQU 38
E_NoMode:				EQU 39+E_EDIT
E_Validation:				EQU 40
E_Length:				EQU 41+E_EDIT
E_Application:				EQU 42+E_EDIT
E_AppErr1:				EQU 43+E_EDIT
E_AppErr2:				EQU 44+E_EDIT
E_ExpiredApp:				EQU 45
E_BadAdd:				EQU 46
E_Archived:				EQU 47+E_EDIT
E_Version:				EQU 48
E_ArchFull:				EQU 49
E_Variable:				EQU 50+E_EDIT
E_Duplicate:				EQU 51+E_EDIT
HigErrNum:				EQU 51
;Obsolete error numbers 34 ;first LINK error
E_LinkIOChkSum:				EQU 34
E_LinkIOTimeOut:				EQU 35
E_LinkIOBusy:				EQU 36
E_LinkIOVer:				EQU 37

;Equates To RAM Locations For Stat Vars
;----------------------------------------------------------------
FPLEN:				EQU 9 ;Length of a floating-point number.
StatN:				EQU statVars
XMean:				EQU StatN + FPLEN
SumX:				EQU XMean + FPLEN
SumXSqr:				EQU SumX + FPLEN
StdX:				EQU SumXSqr + FPLEN
StdPX:				EQU StdX + FPLEN
MinX:				EQU StdPX + FPLEN
MaxX:				EQU MinX + FPLEN
MinY:				EQU MaxX + FPLEN
MaxY:				EQU MinY + FPLEN
YMean:				EQU MaxY + FPLEN
SumY:				EQU YMean + FPLEN
SumYSqr:				EQU SumY + FPLEN
StdY:				EQU SumYSqr + FPLEN
StdPY:				EQU StdY + FPLEN
SumXY:				EQU StdPY + FPLEN
Corr:				EQU SumXY + FPLEN
MedX:				EQU Corr + FPLEN
Q1:				EQU MedX + FPLEN
Q3:				EQU Q1 + FPLEN
QuadA:				EQU Q3 + FPLEN
QuadB:				EQU QuadA + FPLEN
QuadC:				EQU QuadB + FPLEN
CubeD:				EQU QuadC + FPLEN
QuartE:				EQU CubeD + FPLEN
MedX1:				EQU QuartE + FPLEN
MedX2:				EQU MedX1 + FPLEN
MedX3:				EQU MedX2 + FPLEN
MedY1:				EQU MedX3 + FPLEN
MedY2:				EQU MedY1 + FPLEN
MedY3:				EQU MedY2 + FPLEN
PStat:				EQU MedY3 + 2*FPLEN
ZStat:				EQU PStat + FPLEN
TStat:				EQU ZStat + FPLEN
ChiStat:				EQU TStat + FPLEN
FStat:				EQU ChiStat + FPLEN
DF:				EQU FStat + FPLEN
Phat:				EQU DF + FPLEN
Phat1:				EQU Phat + FPLEN
Phat2:				EQU Phat1 + FPLEN
MeanX1:				EQU Phat2 + FPLEN
StdX1:				EQU MeanX1 + FPLEN
StatN1:				EQU StdX1 + FPLEN
MeanX2:				EQU StatN1 + FPLEN
StdX2:				EQU MeanX2 + FPLEN
StatN2:				EQU StdX2 + FPLEN
StdXP2:				EQU StatN2 + FPLEN
SLower:				EQU StdXP2 + FPLEN
SUpper:				EQU SLower + FPLEN
SStat:				EQU SUpper + FPLEN
F_DF:				EQU anovaf_vars
F_SS:				EQU F_DF + FPLEN
F_MS:				EQU F_SS + FPLEN
E_DF:				EQU F_MS + FPLEN
E_SS:				EQU E_DF + FPLEN
E_MS:				EQU E_SS + FPLEN
