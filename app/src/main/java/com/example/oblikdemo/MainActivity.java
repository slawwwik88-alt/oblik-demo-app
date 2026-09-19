package com.example.oblikdemo;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.graphics.*;
import android.os.Bundle;
import android.view.*;
import android.view.animation.DecelerateInterpolator;
import java.util.Locale;

public class MainActivity extends Activity {

    DemoView demo;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_FULLSCREEN |
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
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
        final int BG=Color.rgb(231,228,210);
        final int CARD=Color.rgb(226,223,204);
        final int MUTED=Color.rgb(95,95,87);
        final int LINE=Color.rgb(151,149,135);
        final int TICKER=Color.rgb(119,84,25);
        final int ORANGE=Color.rgb(255,137,0);
        final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
        final Paint stroke=new Paint(Paint.ANTI_ALIAS_FLAG);
        final Typeface regular=Typeface.create("sans-serif",Typeface.NORMAL);
        final Typeface medium=Typeface.create("sans-serif-medium",Typeface.NORMAL);
        final Typeface condensed=Typeface.create("sans-serif-condensed",Typeface.NORMAL);
        final Typeface condensedMedium=Typeface.create("sans-serif-condensed",Typeface.BOLD);
        final Path path=new Path();

        enum Screen { MAIN, DETAILS, QR, SERVICES, JOBS, MENU }
        Screen current=Screen.MAIN, from=Screen.MAIN, to=Screen.MAIN;
        float transition=1f;
        int direction=1;
        ValueAnimator animator;
        float downX,downY,lastY,detailsScroll=0;
        boolean dragging=false;
        long lastTap=0;

        DemoView() {
            super(MainActivity.this);
            setLayerType(View.LAYER_TYPE_SOFTWARE,null);
            stroke.setStyle(Paint.Style.STROKE);
            stroke.setStrokeWidth(1.5f);
            stroke.setColor(LINE);
        }

        float sx(){return getWidth()/W;}
        float sy(){return getHeight()/H;}

        @Override protected void onDraw(Canvas real) {
            super.onDraw(real);
            real.save();
            real.scale(sx(),sy());

            if (transition >= .999f) {
                drawScreen(real,current,255,0);
            } else {
                int a0=(int)(255*(1-transition));
                int a1=(int)(255*transition);
                drawScreen(real,from,a0,-direction*44f*transition);
                drawScreen(real,to,a1,direction*70f*(1-transition));
            }
            real.restore();
            postInvalidateDelayed(33);
        }

        void drawScreen(Canvas c, Screen s, int alpha, float dx) {
            c.save();
            c.translate(dx,0);
            int sc=c.saveLayerAlpha(0,0,W,H,alpha);
            p.setStyle(Paint.Style.FILL); p.setColor(BG); c.drawRect(0,0,W,H,p);
            drawStatus(c);
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
            roundStroke(c,40,357,644,1211,12,CARD,LINE,1.6f);

            text(c,"Резерв ID",68,431,39,Color.BLACK,regular);
            drawShield(c,560,396,54,61);

            text(c,"Дата народження:",67,499,25,MUTED,regular);
            text(c,"10.08.1993",67,536,29,Color.BLACK,regular);

            // large visual breathing room exactly as reference
            drawTicker(c,41,928,643,970);

            text(c,"Військовозобов’язаний",67,1045,25,MUTED,regular);
            text(c,"ТЕЛЬНИХ",67,1098,39,Color.BLACK,condensed);
            text(c,"СВЯТОСЛАВ",67,1137,39,Color.BLACK,condensed);
            text(c,"Олександрович",67,1177,39,Color.BLACK,condensed);

            p.setColor(ORANGE);p.setStyle(Paint.Style.FILL);c.drawCircle(583,1150,33,p);
            p.setStrokeWidth(4);p.setStrokeCap(Paint.Cap.SQUARE);p.setColor(Color.BLACK);
            c.drawLine(568,1150,598,1150,p);c.drawLine(583,1135,583,1165,p);

            drawBottom(c,Screen.MAIN);
        }

        void drawTicker(Canvas c,float l,float t,float r,float b){
            p.setStyle(Paint.Style.FILL);p.setColor(TICKER);c.drawRect(l,t,r,b,p);
            c.save();c.clipRect(l,t,r,b);
            String s="DEMO • Оновлено о 15:47 | 17.09.2026 • Документ не є офіційним • ";
            p.setTypeface(medium);p.setTextSize(20);p.setColor(Color.WHITE);
            float sw=p.measureText(s), off=-(System.currentTimeMillis()/28f)%sw;
            c.drawText(s,off,t+29,p);c.drawText(s,off+sw,t+29,p);c.drawText(s,off+2*sw,t+29,p);
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
            drawShield(c,579,210,60,70);
            drawTicker(c,0,319,684,361);

            round(c,40,401,644,817,26,Color.WHITE);
            text(c,"ТЕЛЬНИХ",67,462,37,Color.BLACK,condensed);
            text(c,"СВЯТОСЛАВ",67,502,37,Color.BLACK,condensed);
            text(c,"Олександрович",67,542,37,Color.BLACK,condensed);
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
            p.setStyle(Paint.Style.FILL);p.setColor(Color.WHITE);c.drawRect(0,1363,W,H,p);
            float[] cx={81,253,429,603};
            String[] labs={"Резерв ID","Сервіси","Вакансії","Меню"};
            Screen[] ss={Screen.MAIN,Screen.SERVICES,Screen.JOBS,Screen.MENU};
            for(int i=0;i<4;i++){
                boolean a=active==ss[i];
                drawNavIcon(c,cx[i],1414,i,a);
                textCentered(c,labs[i],cx[i],1466,22,Color.BLACK,a?medium:regular);
            }
            round(c,252,1514,432,1520,3,Color.rgb(96,96,96));
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

        void drawShield(Canvas c,float x,float y,float w,float h){
            path.reset();path.moveTo(x,y);path.lineTo(x+w,y);path.lineTo(x+w,y+h*.68f);path.lineTo(x+w*.5f,y+h);path.lineTo(x,y+h*.68f);path.close();
            p.setStyle(Paint.Style.FILL);p.setColor(Color.BLACK);c.drawPath(path,p);
            textCentered(c,"D",x+w/2,y+h*.68f,25,Color.WHITE,medium);
        }

        void drawChevron(Canvas c,float x,float y){
            p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(4);p.setStrokeCap(Paint.Cap.SQUARE);p.setColor(Color.BLACK);
            c.drawLine(x,y,x+13,y+14,p);c.drawLine(x+13,y+14,x,y+28,p);
        }

        void text(Canvas c,String s,float x,float base,float size,int color,Typeface tf){
            p.setStyle(Paint.Style.FILL);p.setTypeface(tf);p.setTextSize(size);p.setColor(color);p.setStrokeWidth(1);c.drawText(s,x,base,p);
        }
        void textCentered(Canvas c,String s,float x,float base,float size,int color,Typeface tf){
            p.setTypeface(tf);p.setTextSize(size);p.setColor(color);p.setStyle(Paint.Style.FILL);
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

        boolean goBack(){
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
            float x=e.getX()/sx(), y=e.getY()/sy();
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
                if(current!=Screen.DETAILS && y>1360){switchTo(navFor(x));return true;}
                if(current==Screen.MAIN){
                    if(x>535&&x<635&&y>1090&&y<1215){switchTo(Screen.DETAILS);return true;}
                    if(x>40&&x<644&&y>355&&y<970){
                        long now=System.currentTimeMillis();
                        if(now-lastTap<360){switchTo(Screen.QR);lastTap=0;} else lastTap=now;
                        return true;
                    }
                } else if(current==Screen.DETAILS && y<230){switchTo(Screen.MAIN);return true;}
                else if(current==Screen.QR && y<250){switchTo(Screen.MAIN);return true;}
                return true;
            }
            return true;
        }
    }
}