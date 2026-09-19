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

    enum Screen { LOGIN, MAIN, DETAILS, QR, SERVICES, JOBS, MENU }

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
        final Typeface mid=Typeface.create(regular,450,false);
        final Typeface midLight=Typeface.create(regular,425,false);
        final Typeface titleFace=Typeface.create(regular,480,false);
        final Typeface condensed=Typeface.create("sans-serif-condensed",Typeface.NORMAL);
        final Typeface condensedMedium=Typeface.create("sans-serif-condensed",Typeface.BOLD);
        final Path path=new Path();
        final Bitmap trident;
        final Bitmap titleRef;

        Screen current=Screen.LOGIN, from=Screen.LOGIN, to=Screen.LOGIN;
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
        int enteredDigits=0;

        DemoView() {
            super(MainActivity.this);
            setLayerType(View.LAYER_TYPE_SOFTWARE,null);
            byte[] tridentBytes=Base64.decode("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",Base64.DEFAULT);
            trident=BitmapFactory.decodeByteArray(tridentBytes,0,tridentBytes.length);
            byte[] titleBytes=Base64.decode("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",Base64.DEFAULT);
            titleRef=BitmapFactory.decodeByteArray(titleBytes,0,titleBytes.length);
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
                case LOGIN: drawLogin(c); break;
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

        void drawLogin(Canvas c){
            text(c,"Код для входу",40,205,50,Color.BLACK,regular);

            float[] dx={252,312,372,432};
            for(int i=0;i<4;i++){
                p.setStyle(Paint.Style.FILL);
                p.setColor(Color.WHITE);
                c.drawCircle(dx[i],438,10,p);
                if(i<enteredDigits){
                    p.setColor(Color.BLACK);
                    c.drawCircle(dx[i],438,4.5f,p);
                }
            }

            float[] xs={151,342,532};
            float[] ys={737,912,1087};
            int n=1;
            for(int r=0;r<3;r++){
                for(int col=0;col<3;col++){
                    p.setColor(Color.WHITE);p.setStyle(Paint.Style.FILL);c.drawCircle(xs[col],ys[r],72,p);
                    textCentered(c,String.valueOf(n++),xs[col],ys[r]+22,56,Color.BLACK,regular);
                }
            }
            p.setColor(Color.WHITE);c.drawCircle(342,1262,72,p);
            textCentered(c,"0",342,1284,56,Color.BLACK,regular);

            Path back=new Path();
            back.moveTo(525,1238); back.lineTo(565,1238);
            back.quadTo(572,1238,572,1247);
            back.lineTo(572,1277);
            back.quadTo(572,1286,563,1286);
            back.lineTo(525,1286);
            back.lineTo(502,1262);
            back.close();
            p.setColor(Color.WHITE);c.drawPath(back,p);
            p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(4);p.setStrokeCap(Paint.Cap.ROUND);p.setColor(Color.BLACK);
            c.drawLine(531,1252,550,1271,p); c.drawLine(550,1252,531,1271,p);
            p.setStyle(Paint.Style.FILL);

            textCentered(c,"Не пам’ятаю код для входу",342,1414,30,Color.BLACK,regular);
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
            drawReferenceTrident(c,548,386,74,88);

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
            p.setTypeface(medium);p.setTextSize(22);p.setTextScaleX(1.0f);p.setColor(Color.WHITE);
            float sw=p.measureText(s), off=-(tickerOffset%sw);
            c.drawText(s,off,t+30,p);c.drawText(s,off+sw,t+30,p);c.drawText(s,off+2*sw,t+30,p);p.setTextScaleX(1f);
            c.restore();
        }

        void drawDetails(Canvas c){
            // dim background above the sheet
            p.setColor(Color.rgb(160,159,148));p.setStyle(Paint.Style.FILL);c.drawRect(0,0,W,112,p);
            p.setColor(BG);c.drawRoundRect(0,110,W,1600,38,38,p);
            round(c,309,136,376,143,4,Color.BLACK);
            drawDemoPill(c,158);

            c.save();
            c.translate(0,-detailsScroll);

            text(c,"Резерв ID",42,263,50,Color.BLACK,regular);
            drawReferenceTrident(c,566,198,78,93);
            drawTicker(c,0,319,684,361);
            round(c,40,402,644,817,26,Color.WHITE);
            textFio(c,"ТЕЛЬНИХ",67,462,37);
            textFio(c,"СВЯТОСЛАВ",67,502,37);
            textFio(c,"Олександрович",67,542,37);
            text(c,"Військовозобов’язаний",67,598,27,Color.BLACK,medium);
            text(c,"Дата народження:",67,650,27,Color.BLACK,medium);
            text(c,"10.08.1993",67,693,27,Color.BLACK,regular);
            text(c,"РНОКПП:",67,744,27,Color.BLACK,medium);
            text(c,"0000000000",67,787,27,Color.BLACK,regular);

            round(c,40,830,644,1429,26,Color.WHITE);
            text(c,"ТЦК та СП:",67,881,26,Color.BLACK,medium);
            text(c,"Індустріальний районний у місті Дніпро",67,922,24,Color.BLACK,regular);
            text(c,"ТЦК та СП",67,949,24,Color.BLACK,regular);
            line(c,40,981,644,981,Color.rgb(225,223,214),1);
            text(c,"Звання",67,1031,25,Color.BLACK,regular);
            text(c,"Солдат",397,1031,25,Color.BLACK,regular);
            text(c,"ВОС:",67,1083,25,Color.BLACK,regular);
            text(c,"999097",397,1083,25,Color.BLACK,regular);
            text(c,"Категорія обліку:",67,1139,25,Color.BLACK,regular);
            text(c,"Військовозобов’язаний",67,1181,25,Color.BLACK,regular);
            text(c,"Потребує проходження базової",67,1235,23,Color.BLACK,regular);
            text(c,"загальновійськової підготовки,Солдат",67,1264,23,Color.BLACK,regular);
            text(c,"резерву",67,1293,23,Color.BLACK,regular);
            text(c,"Номер в реєстрі Оберіг:",67,1344,24,Color.BLACK,medium);
            text(c,"DEMO-000000000000000000000",67,1387,24,Color.BLACK,regular);

            round(c,40,1443,644,1605,26,Color.WHITE);
            text(c,"Телефон:",67,1493,26,Color.BLACK,medium);
            text(c,"+380 00 000 0000",67,1535,25,Color.BLACK,regular);

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
            p.setSubpixelText(true);
            p.setLinearText(true);
            p.setStyle(Paint.Style.FILL);
            p.setTypeface(titleFace);
            p.setTextSize(39);
            p.setTextScaleX(0.985f);
            p.setColor(Color.BLACK);
            c.save();
            c.scale(1f,0.80f,x,y+31f);
            c.drawText("Резерв ID",x,y+31f,p);
            c.restore();
            p.setTextScaleX(1f);
            p.setLinearText(false);
        }

        void drawReferenceTrident(Canvas c,float x,float y,float w,float h){
            if(trident==null) return;
            Rect src=new Rect(0,0,trident.getWidth(),trident.getHeight());
            RectF dst=new RectF(x,y,x+w,y+h);
            p.setAlpha(255);
            p.setFilterBitmap(true);
            c.drawBitmap(trident,src,dst,p);
            p.setFilterBitmap(false);
        }

        void drawChevron(Canvas c,float x,float y){
            p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(4);p.setStrokeCap(Paint.Cap.SQUARE);p.setColor(Color.BLACK);
            c.drawLine(x,y,x+13,y+14,p);c.drawLine(x+13,y+14,x,y+28,p);
        }

        void text(Canvas c,String s,float x,float base,float size,int color,Typeface tf){
            p.setStyle(Paint.Style.FILL);p.setTypeface(tf);p.setTextSize(size);p.setTextScaleX(1f);p.setColor(color);p.setStrokeWidth(1);c.drawText(s,x,base,p);
        }
        void textFio(Canvas c,String s,float x,float base,float size){
            p.setStyle(Paint.Style.FILL);p.setTypeface(midLight);p.setTextSize(size);p.setTextScaleX(1.045f);p.setColor(Color.BLACK);p.setStrokeWidth(1);
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
            if(s==Screen.LOGIN)return -1;
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
            if(current==Screen.LOGIN)return false;
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

                if(current==Screen.LOGIN){
                    int digit=-1;
                    float[] kx={151,342,532};
                    float[] ky={737,912,1087};
                    int val=1;
                    for(int r=0;r<3;r++){
                        for(int col=0;col<3;col++){
                            float ddx=x-kx[col], ddy=y-ky[r];
                            if(ddx*ddx+ddy*ddy<82*82) digit=val;
                            val++;
                        }
                    }
                    float ddx=x-342, ddy=y-1262;
                    if(ddx*ddx+ddy*ddy<82*82) digit=0;
                    if(x>495&&x<590&&y>1215&&y<1310){
                        if(enteredDigits>0) enteredDigits--;
                        invalidate(); return true;
                    }
                    if(digit>=0){
                        if(enteredDigits<4) enteredDigits++;
                        invalidate();
                        if(enteredDigits>=4){
                            postDelayed(()->{enteredDigits=0; switchTo(Screen.MAIN);},180);
                        }
                        return true;
                    }
                    return true;
                }

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