package com.example.oblikdemo;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.graphics.*;
import android.os.Bundle;
import android.util.Base64;
import android.view.*;
import android.view.animation.DecelerateInterpolator;
import java.util.Locale;

public class MainActivity extends Activity {

    enum Screen { MAIN, DETAILS, QR, SERVICES, JOBS, MENU }

    DemoView demo;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().setStatusBarColor(Color.rgb(225,222,203));
        getWindow().setNavigationBarColor(Color.WHITE);
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR |
            View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        );
        demo = new DemoView();
        setContentView(demo);
    }

    @Override public void onBackPressed() {
        if (demo != null && demo.goBack()) return;
        super.onBackPressed();
    }

    final class DemoView extends View {
        static final float W=684f,H=1536f;
        static final float TOP_INSET=80f, BOTTOM_INSET=40f, CONTENT_H=H-TOP_INSET-BOTTOM_INSET;
        final int BG=Color.rgb(225,222,203);
        final int CARD=Color.rgb(213,211,190);
        final int MUTED=Color.rgb(95,95,87);
        final int LINE=Color.rgb(151,148,131);
        final int TICKER=Color.rgb(107,75,26);
        final int ORANGE=Color.rgb(255,136,8);
        final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
        final Paint stroke=new Paint(Paint.ANTI_ALIAS_FLAG);
        final Typeface regular=Typeface.create("sans-serif",Typeface.NORMAL);
        final Typeface medium=Typeface.create("sans-serif-medium",Typeface.NORMAL);
        final Typeface condensed=Typeface.create("sans-serif-condensed",Typeface.NORMAL);
        final Typeface condensedMedium=Typeface.create("sans-serif-condensed",Typeface.BOLD);
        final Path path=new Path();
        final Bitmap trident;

        Screen current=Screen.MAIN, from=Screen.MAIN, to=Screen.MAIN;
        float transition=1f;
        int direction=1;
        ValueAnimator animator;
        float downX,downY,lastY,detailsScroll=0;
        boolean dragging=false;
        float tickerOffset=0f;
        long lastFrameNs=0L;
        float cardFlip=0f;
        boolean cardFlipped=false;
        ValueAnimator cardAnimator;
        boolean sheetOpen=false;
        float sheetProgress=0f;
        ValueAnimator sheetAnimator;

        DemoView() {
            super(MainActivity.this);
            setLayerType(View.LAYER_TYPE_SOFTWARE,null);
            byte[] iconBytes=Base64.decode("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",Base64.DEFAULT);
            trident=BitmapFactory.decodeByteArray(iconBytes,0,iconBytes.length);
            stroke.setStyle(Paint.Style.STROKE);
            stroke.setStrokeWidth(1.5f);
            stroke.setColor(LINE);
        }

        float sx(){return getWidth()/W;}
        float sy(){return getHeight()/CONTENT_H;}

        @Override protected void onDraw(Canvas real) {
            super.onDraw(real);
            long now=System.nanoTime();
            if(lastFrameNs!=0L){
                float dt=Math.min(.05f,(now-lastFrameNs)/1_000_000_000f);
                tickerOffset=(tickerOffset+29f*dt)%100000f;
            }
            lastFrameNs=now;

            real.save();
            real.scale(sx(),sy());
            real.translate(0,-TOP_INSET);

            if (transition >= .999f) {
                drawScreen(real,current,255,0);
            } else {
                // only the incoming page is drawn during navigation:
                // this prevents duplicate bars/icons and the visual glitch.
                drawScreen(real,to,255,direction*18f*(1-transition));
            }
            real.restore();
            postInvalidateOnAnimation();
        }

        void drawScreen(Canvas c, Screen s, int alpha, float dx) {
            c.save();
            c.translate(dx,0);
            int sc=c.saveLayerAlpha(0,0,W,H,alpha);
            p.setStyle(Paint.Style.FILL); p.setColor(BG); c.drawRect(0,0,W,H,p);
            switch(s) {
                case MAIN: drawMain(c); break;
                case DETAILS: drawDetails(c); break;
                case QR: drawQr(c); break;
                case SERVICES: drawServices(c); break;
                case JOBS: drawJobs(c); break;
                case MENU: drawMenu(c); break;
            }
            if (s!=Screen.DETAILS) drawDemoPill(c,92);
            c.restoreToCount(sc);
            c.restore();
        }

        void drawStatus(Canvas c) {
            text(c,timeText(),57,61,22,Color.BLACK,medium);
            // simplified signal / wifi / battery, matching footprint of reference
            p.setStrokeWidth(4); p.setStrokeCap(Paint.Cap.ROUND); p.setColor(Color.BLACK);
            c.drawLine(506,58,506,50,p); c.drawLine(514,58,514,46,p);
            c.drawLine(522,58,522,42,p); c.drawLine(530,58,530,38,p);
            p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(3);
            c.drawArc(540,39,573,64,205,130,false,p);
            c.drawArc(547,47,566,62,205,130,false,p);
            p.setStyle(Paint.Style.FILL);c.drawCircle(556,59,2.5f,p);
            round(c,580,39,625,64,8,Color.BLACK);
            text(c,"68",589,60,18,Color.WHITE,medium);
            round(c,627,46,631,58,2,Color.rgb(80,80,80));
            // tiny down arrow
            p.setStrokeWidth(2.5f); p.setColor(Color.BLACK);
            c.drawLine(138,42,138,57,p); c.drawLine(132,52,138,58,p); c.drawLine(144,52,138,58,p);
            c.drawLine(131,63,145,63,p);
        }

        String timeText(){
            java.text.SimpleDateFormat f=new java.text.SimpleDateFormat("HH:mm", Locale.getDefault());
            return f.format(new java.util.Date());
        }

        void drawDemoPill(Canvas c,float y){
            float x=260,w=164,h=36;
            round(c,x,y,x+w,y+h,18,Color.rgb(45,43,38));
            text(c,"DEMO • НЕ ДОКУМЕНТ",276,y+24,13,Color.WHITE,medium);
        }

        void drawNotification(Canvas c){
            round(c,416,149,645,204,28,Color.WHITE);
            text(c,"Сповіщення",438,186,27,Color.BLACK,regular);
            // bell
            p.setStyle(Paint.Style.FILL);p.setColor(Color.BLACK);
            c.drawOval(607,164,628,187,p); c.drawRect(606,177,629,187,p); c.drawCircle(618,191,3,p);
        }

        void drawMain(Canvas c){
            drawNotification(c);

            float scale=Math.abs((float)Math.cos(cardFlip*Math.PI));
            c.save();
            c.scale(Math.max(.025f,scale),1f,342f,784f);
            if(cardFlip<.5f) drawMainFront(c); else drawMainBack(c);
            c.restore();

            drawBottom(c,Screen.MAIN);
            if(sheetProgress>0.001f) drawActionSheet(c);
        }

        void drawMainFront(Canvas c){
            roundStroke(c,40,357,644,1211,12,CARD,LINE,1.6f);

            drawReferenceTitle(c,67,401,190,45);
            drawReferenceTrident(c,560,395,55,65);

            text(c,"Дата народження:",67,499,25,MUTED,regular);
            text(c,"10.08.1993",67,536,29,Color.BLACK,regular);

            drawTicker(c,41,928,643,970);

            text(c,"Військовозобов’язаний",67,1045,25,MUTED,regular);
            // Reference screenshot uses a normal-width sans face, not condensed.
            textFio(c,"ТЕЛЬНИХ",67,1098,39);
            textFio(c,"СВЯТОСЛАВ",67,1137,39);
            textFio(c,"Олександрович",67,1177,39);

            p.setColor(ORANGE);p.setStyle(Paint.Style.FILL);c.drawCircle(583,1150,33,p);
            p.setStrokeWidth(4);p.setStrokeCap(Paint.Cap.SQUARE);p.setColor(Color.BLACK);
            c.drawLine(568,1150,598,1150,p);c.drawLine(583,1135,583,1165,p);
        }

        void drawMainBack(Canvas c){
            roundStroke(c,40,357,644,1211,12,Color.WHITE,LINE,1.6f);
            textCentered(c,"QR-код дійсний до 17 вересня 2027",342,505,25,Color.BLACK,medium);
            drawFakeQr(c,111,552,462);
        }

        void drawTicker(Canvas c,float l,float t,float r,float b){
            p.setStyle(Paint.Style.FILL);p.setColor(TICKER);c.drawRect(l,t,r,b,p);
            c.save();c.clipRect(l,t,r,b);
            String s="Документ оновлено о 15:47 | 17.09.2026 • ";
            p.setTypeface(Typeface.create("sans-serif-medium",Typeface.BOLD));p.setTextSize(22);p.setTextScaleX(1.01f);p.setColor(Color.WHITE);
            float sw=p.measureText(s), off=-(tickerOffset%sw);
            c.drawText(s,off,t+30,p);c.drawText(s,off+sw,t+30,p);c.drawText(s,off+2*sw,t+30,p);p.setTextScaleX(1f);
            c.restore();
        }

        void drawDetails(Canvas c){
            // dim background above the sheet
            p.setColor(Color.rgb(160,159,148));p.setStyle(Paint.Style.FILL);c.drawRect(0,0,W,112,p);
            p.setColor(BG);c.drawRoundRect(0,110,W,1600,38,38,p);
            round(c,309,136,376,143,4,Color.BLACK);
            drawDemoPill(c,157);

            c.save();
            c.translate(0,-detailsScroll);

            text(c,"Резерв ID",42,263,50,Color.BLACK,regular);
            drawReferenceTrident(c,579,210,59,70);
            drawTicker(c,0,319,684,361);

            round(c,40,401,644,817,26,Color.WHITE);
            textFio(c,"ТЕЛЬНИХ",67,462,37);
            textFio(c,"СВЯТОСЛАВ",67,502,37);
            textFio(c,"Олександрович",67,542,37);
            text(c,"Військовозобов’язаний",67,598,27,Color.BLACK,medium);
            text(c,"Дата народження:",67,650,27,Color.BLACK,medium);
            text(c,"10.08.1993",67,693,27,Color.BLACK,regular);
            text(c,"РНОКПП:",67,744,27,Color.BLACK,medium);
            text(c,"0000000000",67,787,27,Color.BLACK,regular);

            round(c,40,829,644,1430,26,Color.WHITE);
            text(c,"ТЦК та СП:",67,881,26,Color.BLACK,medium);
            text(c,"Демонстраційний районний ТЦК та СП",67,922,24,Color.BLACK,regular);
            line(c,40,980,644,980,Color.rgb(225,223,214),1);
            text(c,"Звання",67,1031,25,Color.BLACK,regular);
            text(c,"Солдат",397,1031,25,Color.BLACK,regular);
            text(c,"ВОС:",67,1083,25,Color.BLACK,regular);
            text(c,"000000",397,1083,25,Color.BLACK,regular);
            text(c,"Категорія обліку:",67,1139,25,Color.BLACK,regular);
            text(c,"Військовозобов’язаний",67,1181,25,Color.BLACK,regular);
            text(c,"Потребує проходження базової",67,1235,23,Color.BLACK,regular);
            text(c,"загальновійськової підготовки, Солдат",67,1264,23,Color.BLACK,regular);
            text(c,"резерву",67,1293,23,Color.BLACK,regular);
            text(c,"Номер в реєстрі Оберіг:",67,1345,24,Color.BLACK,medium);
            text(c,"DEMO-000000000000",67,1388,24,Color.BLACK,regular);

            round(c,40,1444,644,1605,26,Color.WHITE);
            text(c,"Телефон:",67,1496,26,Color.BLACK,medium);
            text(c,"+380 00 000 00 00",67,1540,25,Color.BLACK,regular);

            c.restore();
        }

        void drawQr(Canvas c){
            drawNotification(c);
            roundStroke(c,40,357,644,1210,12,Color.WHITE,LINE,1.6f);
            text(c,"QR-код дійсний до 17 вересня 2027",129,504,25,Color.BLACK,medium);
            drawFakeQr(c,111,552,462);
            drawBottom(c,Screen.MAIN);
        }

        void drawFakeQr(Canvas c,float x,float y,float size){
            int n=41;float cell=size/n;
            p.setStyle(Paint.Style.FILL);p.setColor(Color.WHITE);c.drawRect(x,y,x+size,y+size,p);
            for(int yy=0;yy<n;yy++){
                for(int xx=0;xx<n;xx++){
                    int z=(xx*37+yy*19+xx*yy*7+13)%101;
                    boolean on=z<45;
                    if(finderCell(xx,yy,0,0,n)||finderCell(xx,yy,n-7,0,n)||finderCell(xx,yy,0,n-7,n)) on=finderPattern(xx,yy,n);
                    if(on){p.setColor(Color.BLACK);c.drawRect(x+xx*cell,y+yy*cell,x+(xx+1)*cell+.3f,y+(yy+1)*cell+.3f,p);}
                }
            }
            round(c,x+115,y+170,x+347,y+276,12,Color.WHITE);
            strokeRound(c,x+115,y+170,x+347,y+276,12,Color.BLACK,4);
            text(c,"DEMO",x+169,y+239,42,Color.BLACK,medium);
        }
        boolean finderCell(int x,int y,int fx,int fy,int n){return x>=fx&&x<fx+7&&y>=fy&&y<fy+7;}
        boolean finderPattern(int x,int y,int n){
            int fx=x>=n-7?n-7:0; int fy=y>=n-7?n-7:0;
            int xx=x-fx,yy=y-fy;
            return xx==0||xx==1||xx==5||xx==6||yy==0||yy==1||yy==5||yy==6||(xx>=2&&xx<=4&&yy>=2&&yy<=4);
        }

        void drawServices(Canvas c){
            text(c,"Сервіси",40,203,49,Color.BLACK,regular);
            String[] a={"Виправити дані онлайн","Електронна черга в ТЦК та СП","Запит на відстрочку","Направлення на ВЛК","Розширені дані з реєстру","Стати на облік","Уточнити контактні дані","Штрафи"};
            float y=301;
            for(int i=0;i<a.length;i++){
                text(c,a[i],40,y,30,Color.BLACK,regular);
                drawChevron(c,619,y-22);
                if(i<a.length-1) line(c,40,y+40,644,y+40,Color.rgb(164,163,150),1.2f);
                y+=102;
            }
            drawBottom(c,Screen.SERVICES);
        }

        void drawJobs(Canvas c){
            strokeCircle(c,604,157,20,Color.BLACK,2.5f);
            text(c,"?",597,165,22,Color.BLACK,medium);
            text(c,"Вакансії",40,281,51,Color.BLACK,regular);
            String[] lines={
                "Тут знаходяться актуальні посади для",
                "служби в українському війську, надані",
                "у співпраці з платформою Lobby X.",
                "",
                "Це найбільший перелік пропозицій,",
                "який допоможе знайти ту, що",
                "підходить саме вам. Обирайте",
                "варіанти, подавайте заявки у кілька",
                "кліків і очікуйте відповіді від бригади"
            };
            float y=359;
            for(String s:lines){ if(s.length()>0) text(c,s,40,y,28,Color.BLACK,regular); y+=34; }

            strokeRect(c,171,1128,202,1159,Color.BLACK,2.5f);
            text(c,"Більше не показувати",235,1157,24,Color.BLACK,regular);
            round(c,40,1218,644,1324,53,ORANGE);
            textCentered(c,"Почати",342,1285,31,Color.BLACK,regular);
            drawBottom(c,Screen.JOBS);
        }

        void drawMenu(Canvas c){
            text(c,"Меню",34,178,51,Color.BLACK,regular);
            text(c,"Версія 2.4.1 • DEMO",34,222,22,Color.rgb(169,166,150),regular);

            round(c,33,252,651,456,28,Color.WHITE);
            drawMenuRow(c,33,252,651,354,"▯","Активні сесії",true);
            drawMenuRow(c,33,354,651,456,"⚙","Налаштування",true);

            round(c,33,468,651,765,28,Color.WHITE);
            drawMenuRow(c,33,468,651,568,"?","Питання та відповіді",true);
            drawMenuRow(c,33,568,651,665,"⌁","Служба підтримки",true);
            drawMenuRow(c,33,665,651,765,"▣","Копіювати номер пристрою",false);

            round(c,33,779,651,885,28,Color.WHITE);
            drawMenuRow(c,33,779,651,885,"⌗","Сканувати документ",false);

            round(c,267,925,419,1007,42,Color.rgb(39,37,32));
            textCentered(c,"Вийти",343,978,29,Color.WHITE,regular);
            textCentered(c,"Повідомлення про обробку",342,1052,20,Color.BLACK,regular);
            textCentered(c,"персональних даних",342,1077,20,Color.BLACK,regular);
            line(c,190,1057,493,1057,Color.BLACK,1);
            line(c,226,1082,458,1082,Color.BLACK,1);
            drawBottom(c,Screen.MENU);
        }

        void drawMenuRow(Canvas c,float l,float top,float r,float bottom,String icon,String label,boolean arrow){
            float mid=(top+bottom)/2;
            text(c,icon,l+38,mid+10,30,Color.BLACK,regular);
            text(c,label,l+100,mid+11,31,Color.BLACK,regular);
            if(arrow) drawChevron(c,r-47,mid-16);
            if(bottom<765 && bottom!=456) line(c,l+34,bottom,r-34,bottom,Color.rgb(225,223,214),1);
        }

        void drawBottom(Canvas c,Screen active){
            p.setStyle(Paint.Style.FILL);p.setColor(Color.WHITE);c.drawRect(0,1363,W,1496,p);
            float[] cx={81,253,429,603};
            String[] labs={"Резерв ID","Сервіси","Вакансії","Меню"};
            Screen[] ss={Screen.MAIN,Screen.SERVICES,Screen.JOBS,Screen.MENU};
            for(int i=0;i<4;i++){
                boolean a=active==ss[i];
                drawNavIcon(c,cx[i],1414,i,a);
                textCentered(c,labs[i],cx[i],1466,22,Color.BLACK,a?medium:regular);
            }

        }

        void drawNavIcon(Canvas c,float cx,float cy,int type,boolean active){
            p.setStrokeWidth(3);p.setStyle(Paint.Style.STROKE);p.setColor(Color.BLACK);
            if(type==0){
                if(active){round(c,cx-17,cy-21,cx+17,cy+21,3,Color.BLACK);p.setColor(Color.WHITE);}
                else {strokeRound(c,cx-17,cy-21,cx+17,cy+21,3,Color.BLACK,3);p.setColor(Color.BLACK);}
                p.setStrokeWidth(3);c.drawLine(cx-10,cy+5,cx+10,cy+5,p);c.drawLine(cx-10,cy+12,cx+10,cy+12,p);
            } else if(type==1){
                for(int yy=0;yy<2;yy++)for(int xx=0;xx<2;xx++){
                    float x=cx-18+xx*22,y=cy-18+yy*22;
                    if(active) round(c,x,y,x+15,y+15,3,Color.BLACK); else strokeRound(c,x,y,x+15,y+15,3,Color.BLACK,3);
                }
            } else if(type==2){
                strokeRound(c,cx-17,cy-20,cx+17,cy+20,2,Color.BLACK,3);
                Path d=new Path();d.moveTo(cx,cy-10);d.lineTo(cx+10,cy);d.lineTo(cx,cy+10);d.lineTo(cx-10,cy);d.close();
                p.setStyle(Paint.Style.FILL);p.setColor(active?Color.BLACK:Color.rgb(50,50,50));c.drawPath(d,p);
            } else {
                p.setColor(Color.BLACK);p.setStrokeWidth(4);p.setStrokeCap(Paint.Cap.SQUARE);
                c.drawLine(cx-15,cy-12,cx+15,cy-12,p);c.drawLine(cx-15,cy,cx+15,cy,p);c.drawLine(cx-15,cy+12,cx+15,cy+12,p);
            }
        }

        void drawReferenceTitle(Canvas c,float x,float y,float w,float h){
            // Tuned against the user's 684x1536 reference: slightly narrower and lighter than Android default.
            c.save();
            c.scale(0.945f,1f,x,y);
            text(c,"Резерв ID",x,y+31,39,Color.BLACK,regular);
            c.restore();
        }

        void drawReferenceTrident(Canvas c,float x,float y,float w,float h){
            // Shield + trident proportions traced from the supplied screenshot.
            Path sh=new Path();
            sh.moveTo(x,y); sh.lineTo(x+w,y); sh.lineTo(x+w,y+h*.63f);
            sh.quadTo(x+w*.92f,y+h*.82f,x+w*.50f,y+h);
            sh.quadTo(x+w*.08f,y+h*.82f,x,y+h*.63f); sh.close();
            p.setStyle(Paint.Style.FILL); p.setColor(Color.BLACK); c.drawPath(sh,p);

            p.setColor(Color.rgb(225,222,203));
            p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(5.2f); p.setStrokeCap(Paint.Cap.ROUND); p.setStrokeJoin(Paint.Join.ROUND);
            float cx=x+w*.5f;
            Path t=new Path();
            t.moveTo(cx,y+h*.72f); t.lineTo(cx,y+h*.27f);
            t.moveTo(cx,y+h*.39f); t.cubicTo(cx-w*.13f,y+h*.31f,cx-w*.18f,y+h*.22f,cx-w*.18f,y+h*.10f);
            t.moveTo(cx,y+h*.39f); t.cubicTo(cx+w*.13f,y+h*.31f,cx+w*.18f,y+h*.22f,cx+w*.18f,y+h*.10f);
            t.moveTo(cx-w*.18f,y+h*.10f); t.lineTo(cx-w*.18f,y+h*.58f);
            t.moveTo(cx+w*.18f,y+h*.10f); t.lineTo(cx+w*.18f,y+h*.58f);
            t.moveTo(cx-w*.18f,y+h*.58f); t.quadTo(cx-w*.08f,y+h*.67f,cx,y+h*.72f);
            t.moveTo(cx+w*.18f,y+h*.58f); t.quadTo(cx+w*.08f,y+h*.67f,cx,y+h*.72f);
            c.drawPath(t,p);
            p.setStyle(Paint.Style.FILL);
        }

        void drawChevron(Canvas c,float x,float y){
            p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(4);p.setStrokeCap(Paint.Cap.SQUARE);p.setColor(Color.BLACK);
            c.drawLine(x,y,x+13,y+14,p);c.drawLine(x+13,y+14,x,y+28,p);
        }

        void text(Canvas c,String s,float x,float base,float size,int color,Typeface tf){
            p.setStyle(Paint.Style.FILL);p.setTypeface(tf);p.setTextSize(size);p.setTextScaleX(1f);p.setColor(color);p.setStrokeWidth(1);c.drawText(s,x,base,p);
        }
        void textFio(Canvas c,String s,float x,float base,float size){
            p.setStyle(Paint.Style.FILL);p.setTypeface(regular);p.setTextSize(size);p.setTextScaleX(1.065f);p.setColor(Color.BLACK);p.setStrokeWidth(1);
            c.drawText(s,x,base,p);
            p.setTextScaleX(1f);
        }
        void textCentered(Canvas c,String s,float x,float base,float size,int color,Typeface tf){
            p.setTypeface(tf);p.setTextSize(size);p.setTextScaleX(1f);p.setColor(color);p.setStyle(Paint.Style.FILL);
            c.drawText(s,x-p.measureText(s)/2f,base,p);
        }
        void round(Canvas c,float l,float t,float r,float b,float rad,int color){
            p.setStyle(Paint.Style.FILL);p.setColor(color);c.drawRoundRect(l,t,r,b,rad,rad,p);
        }
        void roundStroke(Canvas c,float l,float t,float r,float b,float rad,int fill,int line,float sw){
            round(c,l,t,r,b,rad,fill);strokeRound(c,l,t,r,b,rad,line,sw);
        }
        void strokeRound(Canvas c,float l,float t,float r,float b,float rad,int color,float sw){
            p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(sw);p.setColor(color);c.drawRoundRect(l,t,r,b,rad,rad,p);
        }
        void line(Canvas c,float x1,float y1,float x2,float y2,int color,float sw){
            p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(sw);p.setColor(color);c.drawLine(x1,y1,x2,y2,p);
        }
        void strokeRect(Canvas c,float l,float t,float r,float b,int color,float sw){
            p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(sw);p.setColor(color);c.drawRect(l,t,r,b,p);
        }
        void strokeCircle(Canvas c,float x,float y,float rad,int color,float sw){
            p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(sw);p.setColor(color);c.drawCircle(x,y,rad,p);
        }

        int order(Screen s){
            if(s==Screen.SERVICES)return 1;if(s==Screen.JOBS)return 2;if(s==Screen.MENU)return 3;return 0;
        }

        void switchTo(Screen s){
            if(s==current || (animator!=null&&animator.isRunning())) return;
            from=current;to=s;direction=order(s)>=order(current)?1:-1;transition=0;
            animator=ValueAnimator.ofFloat(0,1);
            animator.setDuration(230);animator.setInterpolator(new DecelerateInterpolator());
            animator.addUpdateListener(a->{transition=(float)a.getAnimatedValue();invalidate();});
            animator.addListener(new android.animation.AnimatorListenerAdapter(){
                @Override public void onAnimationEnd(android.animation.Animator a){current=to;transition=1;invalidate();}
            });
            animator.start();
        }

        void animateCardFlip(){
            if(cardAnimator!=null && cardAnimator.isRunning()) return;
            final float start=cardFlip;
            final float end=cardFlipped?0f:1f;
            cardAnimator=ValueAnimator.ofFloat(start,end);
            cardAnimator.setDuration(520);
            cardAnimator.setInterpolator(new DecelerateInterpolator());
            cardAnimator.addUpdateListener(a->{cardFlip=(float)a.getAnimatedValue();invalidate();});
            cardAnimator.addListener(new android.animation.AnimatorListenerAdapter(){
                @Override public void onAnimationEnd(android.animation.Animator a){
                    cardFlip=end;
                    cardFlipped=end>.5f;
                    invalidate();
                }
            });
            cardAnimator.start();
        }

        void setSheet(boolean open){
            if(sheetAnimator!=null && sheetAnimator.isRunning()) sheetAnimator.cancel();
            final float start=sheetProgress;
            final float end=open?1f:0f;
            sheetAnimator=ValueAnimator.ofFloat(start,end);
            sheetAnimator.setDuration(240);
            sheetAnimator.setInterpolator(new DecelerateInterpolator());
            sheetAnimator.addUpdateListener(a->{sheetProgress=(float)a.getAnimatedValue();invalidate();});
            sheetAnimator.addListener(new android.animation.AnimatorListenerAdapter(){
                @Override public void onAnimationEnd(android.animation.Animator a){
                    sheetProgress=end; sheetOpen=open; invalidate();
                }
            });
            sheetAnimator.start();
        }

        void drawActionSheet(Canvas c){
            int alpha=(int)(105*sheetProgress);
            p.setStyle(Paint.Style.FILL); p.setColor(Color.argb(alpha,0,0,0));
            c.drawRect(0,0,W,H,p);

            float sheetTop=1070f + (1f-sheetProgress)*470f;
            float bottom=1536f;
            p.setColor(Color.WHITE);
            RectF rr=new RectF(26,sheetTop,658,bottom);
            c.drawRoundRect(rr,28,28,p);
            // square off the lower corners like a bottom sheet
            c.drawRect(26,sheetTop+28,658,bottom,p);

            round(c,309,sheetTop+33,375,sheetTop+40,4,Color.BLACK);

            drawInfoIcon(c,84,sheetTop+114);
            text(c,"Переглянути документ",132,sheetTop+126,30,Color.BLACK,medium);

            drawDocIcon(c,85,sheetTop+206);
            text(c,"Завантажити PDF",132,sheetTop+218,30,Color.BLACK,medium);

            drawRefreshIcon(c,85,sheetTop+303);
            text(c,"Оновити документ",132,sheetTop+315,30,Color.BLACK,medium);
        }

        void drawInfoIcon(Canvas c,float x,float y){
            strokeCircle(c,x,y,19,Color.BLACK,3);
            textCentered(c,"i",x,y+9,28,Color.BLACK,medium);
        }

        void drawDocIcon(Canvas c,float x,float y){
            p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(3);p.setColor(Color.BLACK);
            RectF r=new RectF(x-13,y-20,x+13,y+20);c.drawRoundRect(r,3,3,p);
            c.drawLine(x+2,y-20,x+13,y-9,p); c.drawLine(x+2,y-20,x+2,y-9,p); c.drawLine(x+2,y-9,x+13,y-9,p);
            c.drawLine(x-7,y+3,x+7,y+3,p); c.drawLine(x-7,y+10,x+7,y+10,p);
        }

        void drawRefreshIcon(Canvas c,float x,float y){
            p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(3);p.setColor(Color.BLACK);p.setStrokeCap(Paint.Cap.ROUND);
            c.drawArc(x-17,y-17,x+17,y+17,35,245,false,p);
            c.drawArc(x-17,y-17,x+17,y+17,215,145,false,p);
            p.setStyle(Paint.Style.FILL);
            Path a=new Path();a.moveTo(x+15,y-15);a.lineTo(x+19,y-3);a.lineTo(x+7,y-6);a.close();c.drawPath(a,p);
            Path b=new Path();b.moveTo(x-15,y+15);b.lineTo(x-19,y+3);b.lineTo(x-7,y+6);b.close();c.drawPath(b,p);
        }

        boolean goBack(){
            if(sheetOpen || sheetProgress>0.01f){setSheet(false);return true;}
            if(current!=Screen.MAIN){switchTo(Screen.MAIN);return true;}
            return false;
        }

        Screen navFor(float x){
            if(x<W*.25f)return Screen.MAIN;
            if(x<W*.50f)return Screen.SERVICES;
            if(x<W*.75f)return Screen.JOBS;
            return Screen.MENU;
        }

        @Override public boolean onTouchEvent(android.view.MotionEvent e){
            float x=e.getX()/sx(), y=e.getY()/sy()+TOP_INSET;
            if(e.getAction()==MotionEvent.ACTION_DOWN){
                downX=x;downY=y;lastY=y;dragging=false;return true;
            }
            if(e.getAction()==MotionEvent.ACTION_MOVE){
                if(current==Screen.DETAILS){
                    float dy=y-lastY;
                    if(Math.abs(y-downY)>8)dragging=true;
                    detailsScroll=Math.max(0,Math.min(260,detailsScroll-dy));
                    lastY=y;invalidate();
                }
                return true;
            }
            if(e.getAction()==MotionEvent.ACTION_UP){
                if(dragging)return true;

                if(current==Screen.MAIN && (sheetOpen || sheetProgress>0.01f)){
                    float top=1070f;
                    if(y<top){setSheet(false);return true;}
                    if(y>top+70 && y<top+170){setSheet(false);switchTo(Screen.DETAILS);return true;}
                    if(y>top+170 && y<top+270){return true;}
                    if(y>top+270 && y<top+370){setSheet(false);tickerOffset=0;return true;}
                    return true;
                }

                if(current!=Screen.DETAILS && y>1360){switchTo(navFor(x));return true;}
                if(current==Screen.MAIN){
                    if(!cardFlipped && x>535&&x<635&&y>1090&&y<1215){
                        setSheet(true);return true;
                    }
                    if(x>40&&x<644&&y>355&&y<1211){
                        animateCardFlip();return true;
                    }
                } else if(current==Screen.DETAILS && y<230){switchTo(Screen.MAIN);return true;}
                else if(current==Screen.QR && y<250){switchTo(Screen.MAIN);return true;}
                return true;
            }
            return true;
        }
    }
}