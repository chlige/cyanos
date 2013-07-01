// Menu G5.5.1 (frame)
// Last Modified: Jul. 03, 2004
// Web Site: yxScripts.com
// Email: m_yangxin@hotmail.com

// Copyright 2003, 2004  Xin Yang   All Rights Reserved.

var _assumeFrameset=1;
var _framesetURL="http://www.yxscripts.com/menuG5";

if( typeof(assumeFrameset)=="undefined" ) { assumeFrameset=_assumeFrameset };
if( typeof(framesetURL)=="undefined" ) { framesetURL=_framesetURL };

var yx_ii=yx_ih=yx_bv=yx_bu=yx_ie=yx_ij=yx_bt=yx_bw=yx_id=yx_bs=null;
var yx_fr=yx_ft=null;
var yx_ow=yx_oh=0;
var yx_FS=yx_cu();
var setSubFrame=yx_FS.setSubFrame,setSubID=yx_FS.setSubID,setSubFrameName=yx_FS.setSubFrameName;
var addMenu=yx_FS.addMenu,addSubMenu=yx_FS.addSubMenu,addLink=yx_FS.addLink,addCommand=yx_FS.addCommand,addInfo=yx_FS.addInfo,addSeparator=yx_FS.addSeparator,endMenu=yx_FS.endMenu,addInstance=yx_FS.addInstance;
var addStylePad=yx_FS.addStylePad,addStyleItem=yx_FS.addStyleItem,addStyleFont=yx_FS.addStyleFont,addStyleTag=yx_FS.addStyleTag,addStyleIcon=yx_FS.addStyleIcon,addStyleSeparator=yx_FS.addStyleSeparator,addStyleMenu=yx_FS.addStyleMenu,addStyleGroup=yx_FS.addStyleGroup;
var addItemEvent=yx_FS.addItemEvent,addMenuEvent=yx_FS.addMenuEvent,addWindowEvent=yx_FS.addWindowEvent;
var showMenu=yx_FS.showMenu,showMenuX=yx_FS.showMenuX,hideMenu=yx_FS.hideMenu,hideMenuX=yx_FS.hideMenuX,openMenu=yx_FS.openMenu,openMenuX=yx_FS.openMenuX,closeMenu=yx_FS.closeMenu,closeMenuX=yx_FS.closeMenuX,closeMenuNow=yx_FS.closeMenuNow,moveMenuTo=yx_FS.moveMenuTo,moveMenuBy=yx_FS.moveMenuBy,moveMenuBack=yx_FS.moveMenuBack,setCoordinates=yx_FS.setCoordinates,getMenuDim=yx_FS.getMenuDim,slideMenuBack=yx_FS.slideMenuBack,showPagePath=yx_FS.showPagePath,resetPagePath=yx_FS.resetPagePath,updateItemDisplay=yx_FS.updateItemDisplay,updateItemLink=yx_FS.updateItemLink,updateItemCode=yx_FS.updateItemCode;
function initMenu(cy,dp,f) { yx_FS.initMenu(cy,dp,f||self); }
function initSub(cy){ yx_FS.initSub(cy,self); }
function getLeft(id) {
	var l=yx_FS.yx_ck(self,id);
	return l==null?0:(yx_FS.yx_db(l));
}
function getTop(id){
	var l=yx_FS.yx_ck(self,id);
	return l==null?0:(yx_FS.yx_dq(l));
}
function clickMenu(cy){yx_FS.clickMenu(cy,self);}

function clickMenuX(cy){yx_FS.clickMenuX(cy,self);}
function switchMenu(cy){yx_FS.switchMenu(cy,self);}
function setHolder(n,id){yx_FS.setHolder(n,id,self);}
function yx_cu(){
	var fs=parent;
	while (fs!=top&&typeof(fs.yx_menuSafe)=="undefined") {
		fs=fs.parent;
	}
	if(fs==self||typeof(fs.yx_menuSafe)=="undefined") {
		if(assumeFrameset==1) {
			fs=top;
		} else {
			top.location.replace(framesetURL);
		}
	}
	return fs;
}
function yx_ic() {return true; }
function yx_aw(e){ if(yx_FS.yx_menuSafe){if(yx_FS.yx_isGecko){if(e.gg=="click"){var db=yx_FS.yx_ej;yx_FS.yx_aw();if(!db){yx_bs(e);yx_id(e)}}else if(e.gg=="mousedown"&&!yx_FS.yx_ej){yx_bt(e);yx_ie(e)}else if(e.gg=="mouseup"&&!yx_FS.yx_ej){yx_bw(e);yx_ij(e)}}else{yx_FS.yx_aw(1);yx_bs()}}};function yx_bm(){if(yx_FS.yx_menuSafe){if(yx_FS!=self){yx_FS.yx_bm(self)}};yx_ft()};function yx_gg(){if(yx_FS.yx_menuSafe){var nw=yx_FS.yx_ds(self),nh=yx_FS.yx_dr(self);if(yx_ow!=nw||yx_oh!=nh){yx_ow=nw;yx_oh=nh;yx_fr();yx_FS.yx_gg(self)}}};if(yx_FS.yx_isGecko){yx_ie=window.onmousedown?window.onmousedown:yx_ic;yx_ij=window.onmouseup?window.onmouseup:yx_ic;yx_id=window.onclick?window.onclick:yx_ic;yx_bt=document.onmousedown?document.onmousedown:yx_ic;yx_bw=document.onmouseup?document.onmouseup:yx_ic;yx_bs=document.onclick?document.onclick:yx_ic;document.onmousedown=yx_aw;document.onmouseup=yx_aw;document.onclick=yx_aw;window.onmousedown=yx_ic;window.onmouseup=yx_ic;window.onclick=yx_ic}else{yx_bs=document.onclick?document.onclick:yx_ic;document.onclick=yx_aw};yx_fr=window.onresize?window.onresize:yx_ic;window.onresize=yx_gg;yx_ft=window.onunload?window.onunload:yx_ic;window.onunload=yx_bm;

