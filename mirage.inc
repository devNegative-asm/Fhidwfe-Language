
; MirageOS Development Include File
;
; Ver. 1.0 - Last Updated: 07/15/00
; By Dan Englender and Jason Kovacs


;=================================;
;======== Ion Subroutines ========;
;=================================;

iversion:              EQU $4083
irandom:               EQU $4086
isprite:               EQU $4089
ilsprite:              EQU $408C
igetpix:               EQU $408F
ifastcopy:             EQU $4092
idetect:               EQU $4095
idecomp:               EQU $4098

;=================================;
;====== MirageOS Subroutines =====;
;=================================;

directin:              EQU $409B
sendbytetios:          EQU $409E
getbytetios:           EQU $40A1
version:               EQU $40A4
setvputs:              EQU $40A7
setpixel:              EQU $40AA
fastcopys:             EQU $40AD
delayb:                EQU $40B0
multhe:                EQU $40B3
multhl:                EQU $40B6
quittoshell:           EQU $40B9
fastline:              EQU $40BC
pixelonhl:             EQU $40BF
pixeloff:              EQU $40C2
pixelxor:              EQU $40C5
pixeltest:             EQU $40C8
pixeloffhl:            EQU $40CB
pixelxorhl:            EQU $40DE
pixeltesthl:           EQU $40D1
fastlineb:             EQU $40D4
fastlinew:             EQU $40D7
fastlinex:             EQU $40DA
pointonc:              EQU $40DD
pointoffc:             EQU $40E0
pointxorc:             EQU $40E3
centertext:            EQU $40E6
cphlbc:                EQU $40E9
putsprite8:            EQU $40EC
fastcopyb:             EQU $40EF
vputsc:                EQU $40F2
scrolld7:              EQU $40F5
vnewline:              EQU $40F8
rand127:               EQU $40FB
disprle:               EQU $40FE
cphlde:                EQU $4101
screentopic:           EQU $4104
fastlined:             EQU $4107
disprlel:              EQU $410A
getnextgoodprog:       EQU $410D
getprevgoodprog:       EQU $4110
getnext:               EQU $4113
getprev:               EQU $4116
compstrs:              EQU $4119
nextstr:               EQU $411C
getinfo:               EQU $411F
fastrectangle:         EQU $4122
gettext:               EQU $4125
gettextv:              EQU $4128
FastRectangle_Save:    EQU $412B
vputa:                 EQU $412E
runprog:               EQU $4131
isgoodprog:            EQU $4134
existfold:             EQU $4137
delprog:               EQU $413A
filledrectangle:       EQU $413D
nextfolder:            EQU $4140
delfolder:             EQU $4143
moveall:               EQU $4149
curfoldname:           EQU $4149
curfoldnamea:          EQU $414C
createfolder:          EQU $414F
compstrsn:             EQU $4152
folder_menu_start:     EQU $4155
options_screen:        EQU $4158
put_folder_name_top:   EQU $415B
general_key_routine:   EQU $415E
find_num_good_progs:   EQU $4161
put_scrollbar:         EQU $4164
invert_lines:          EQU $4167
invert_1_line:         EQU $416A
right_align_value:     EQU $416D
put_mirageos_header:   EQU $4170
put_size_graphic:      EQU $4173
sendprog:              EQU $4176
hideprog:              EQU $4179
arcprog:               EQU $417C
filledrectangle_save:  EQU $417F
getbytetiosw:          EQU $4182
vatswap:               EQU $4185
renameprog:            EQU $4188
renamefolder:          EQU $418B
sysmain:               EQU $418E
setupint:              EQU $4191
move_gui_prog:         EQU $4194
largespritehl:         EQU $4197
Update_Scrollbar:      EQU $419A
Initial_Scrollbar:     EQU $419D
sortallfolds:          EQU $41A0
dofoldsort:            EQU $41A3
getfoldsort:           EQU $41A6
setfoldsort:           EQU $41A9
Increase_Cur_Element:  EQU $41AC
Decrease_Cur_Element:  EQU $41AF
Increase_Max_Elements: EQU $41B2
Decrease_Max_Elements: EQU $41B5
Add_A_To_Cur_Element:  EQU $41B8
Sub_A_From_Cur_Element: EQU $41BB
Add_A_To_Max_Elements: EQU $41BE
Sub_A_From_Max_Elements: EQU $41C1
Skip_Forward_B_From_Top: EQU $41C4
Get_Curgoodprog_Ptr:   EQU $41C7
getchecksum:           EQU $41CA
freearc:               EQU $41CD
swapram:               EQU $41D0
hideall:               EQU $41D3

;=================================;
;====== MirageOS Ram Equates =====;
;=================================;

mlinebitmap:           EQU $FFFF-399
timer1:                EQU $8a3a
timer1max:             EQU $8a3b
timer2:                EQU $8a3c
timer2max:             EQU $8a3d
timer3:                EQU $8a3e
custintaddr:           EQU $966F

saferam1:              EQU SavesScreen
saferam2:              EQU AppBackUpScreen
saferam3:              EQU TempSwapArea

mirflags:              EQU $21
rightalign:            EQU $07
lcdinstport:           EQU $10
lcddataport:           EQU $11

numscrollpixels:       EQU CmdShadow+104
scrollbarcoords:       EQU CmdShadow+105
old_element:           EQU CmdShadow+107
cur_element:           EQU CmdShadow+109
old_max_elem:          EQU CmdShadow+111
max_elements:          EQU CmdShadow+113

; =================================
; ===== Various Other Equates =====
; =================================

gbuf:                  EQU PlotsScreen
LargeSprite:           EQU ilsprite
SmallSprite:           EQU isprite
FastCopy:              EQU ifastcopy

