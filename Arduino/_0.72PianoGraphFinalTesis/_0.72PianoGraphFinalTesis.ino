//*********************** DEFINES, VARIABLES GLOBALES ************************************************
#define canalA 0
#define canalB 1
#define canalC 2
#define canalD 3
#define canalE 4
#define canalF 5
#define canalG 6
#define canalH 7
#define canalI 8
#define canalJ 9
#define canalK 10
#define canalL 11
#define canalM 12
#define canalN 13
#define canalO 14
#define canalP 15
#define resolucionPuntaje 10

byte LENTITUD=50;
byte nota=0;
byte velocidad=0;
byte lectura=0;
boolean banderaT0=1,posibleNotaOff=0;
boolean ledsApagados=0;
int tecla=0;
int teclaInicial=36;
int teclaFinal=95;
unsigned long T0,T1;
boolean notaMatriz[17][60]={0};
boolean vectorNota[60]={0}; //PARA CUANDO HAY SUCESIÓN RAPIDA DE NOTAS ON-OFF
boolean vectorNotaCorta[60]={0}; //para cuando hay una nota corta que no llega a pasar a la primera fila
boolean vectorTeclaPresionada[60]={0};
float tiempoCorrecto=0,tiempoError=0,sumatoriaCorrecto=0,sumatoriaError=0;
float tiempoSumaPrograma=0;
boolean banderaCorrecto=0,banderaError=0;
float porcentajeCorrecto=0;
boolean distinto=1;


//*********************** SETUP ************************************************

void setup()
  {
  for(int i=2;i<14;i++)
    {  
    pinMode(i,OUTPUT);
    }
  
  for(int i=18;i<70;i++)
    {  
    pinMode(i,OUTPUT);
    }
  Serial2.begin(115200);
  Serial3.begin(31250);
  }

//*********************** LOOP ************************************************

void loop() 
  {
  if(banderaT0==1)
    {
    T0=millis();
    T1=T0;
    banderaT0=0;    
    }
    
    prendeMatriz();

      
    
    desplazaFilas();
 
  }

//*********************** FUNCIONES ************************************************
 
void prendeMatriz()
  {
  for(int a=0;a<16;a++)
    {  
    for(int b=2;b<14;b++)
      {
      digitalWrite(b,0);
      }
    for(int b=18;b<54;b++)
     {
     digitalWrite(b,0);
     }
     for(int b=58;b<70;b++)
     {
     digitalWrite(b,0);
     }
     
    PORTF=a;
    
    for(int b=2;b<14;b++)
      {
      digitalWrite(b,notaMatriz[a][b-2]);
      }
    for(int b=18;b<54;b++)
      {
      digitalWrite(b,notaMatriz[a][b-6]);
      }
    for(int b=58;b<70;b++)
      {
      digitalWrite(b,notaMatriz[a][b-10]);
      }     
    }
  }

void desplazaFilas()
  {
  if (millis()-T0 > LENTITUD)
      {
        puntaje();

       
     
      for(int a=16;a>0;a=a-1)
        {
        for(int k=0;k<60;k++)
          {
           notaMatriz[a][k]=notaMatriz[(a-1)][k];
           }
        }


       for(int m=0;m<60;m++)
        {
          if(vectorNotaCorta[m]==1)
            {
             notaMatriz[0][m]=0; //una vez que nos aseguramos que el LED prendido ya se desplazó, mostramos el nota OFF
              vectorNotaCorta[m]=0;
            } 
        }


//REVISAR ESTO!! PUEDE QUE QUEDE UNA COLUMNA SIEMPRE PRENDIDA!!
      
      for(int m=0;m<60;m++)
        {
          if(vectorNota[m]==1)
            {
              notaMatriz[0][m]=1; //una vez que nos aseguramos que el LED apagado ya se desplazó, mostramos el nota ON
              vectorNota[m]=0;
            } 
        } 

        T0=millis();
      }
   
  }
  

//---------------Aplicación----------------------

void serialEvent2() 
  {
    
    lectura= Serial2.read();
    
    switch (lectura)
      {
      
      case (128+canalA):
                    comandoOff(); // hay notaOff
                    break;
      case (144+canalA):
                    comandoOn(); //hay notaOn
                    break;                    
      case (128+canalB):
                    comandoOff(); // hay notaOff
                    break;
      case (144+canalB):
                    comandoOn();
                    break;
      case (128+canalC):
                    comandoOff(); // hay notaOff
                    break;
      case (144+canalC):
                    comandoOn(); //hay notaOn
                    break;  
      case (128+canalD):
                    comandoOff(); // hay notaOff
                    break;
      case (144+canalD):
                    comandoOn(); //hay notaOn
                    break;  
      case (128+canalE):
                    comandoOff(); // hay notaOff
                    break;
      case (144+canalE):
                    comandoOn(); //hay notaOn
                    break;  
      case (128+canalF):
                    comandoOff(); // hay notaOff
                    break;
      case (144+canalF):
                    comandoOn(); //hay notaOn
                    break;  
      case (128+canalG):
                    comandoOff(); // hay notaOff
                    break;
      case (144+canalG):
                    comandoOn(); //hay notaOn
                    break;  
      case (128+canalH):
                    comandoOff(); // hay notaOff
                    break;
      case (144+canalH):
                    comandoOn(); //hay notaOn
                    break;  
     case (128+canalI):
                    comandoOff(); // hay notaOff
                    break;
      case (144+canalI):
                    comandoOn(); //hay notaOn
                    break;  
      case (128+canalJ):
                    comandoOff(); // hay notaOff
                    break;
      case (144+canalJ):
                    comandoOn(); //hay notaOn
                    break;  
      case (128+canalK):
                    comandoOff(); // hay notaOff
                    break;
      case (144+canalK):
                    comandoOn(); //hay notaOn
                    break;  
    case (128+canalL):
                    comandoOff(); // hay notaOff
                    break;
      case (144+canalL):
                    comandoOn(); //hay notaOn
                    break;  
      case (128+canalM):
                    comandoOff(); // hay notaOff
                    break;
      case (144+canalM):
                    comandoOn(); //hay notaOn
                    break;  
     case (128+canalN):
                    comandoOff(); // hay notaOff
                    break;
      case (144+canalN):
                    comandoOn(); //hay notaOn
                    break;  
      case (128+canalO):
                    comandoOff(); // hay notaOff
                    break;
      case (144+canalO):
                    comandoOn(); //hay notaOn
                    break;  
     case (128+canalP):
                    comandoOff(); // hay notaOff
                    break;
      case (144+canalP):
                    comandoOn(); //hay notaOn
                    break;
      case 0xFD:{
                for(int h=0;h<60;h++)
                  {
                    notaMatriz[0][h]=0;
                  }
                break;
                }              
      case 0xFE:{
                LENTITUD=Serial2.read();
                break;
                }
      case 0xFF:{      
                tiempoSumaPrograma=sumatoriaCorrecto+sumatoriaError;
                porcentajeCorrecto=100*sumatoriaCorrecto/tiempoSumaPrograma;
                Serial2.print(porcentajeCorrecto);
                Serial2.print('~'); //con esto damos a conocer la finalización del String de datos
                Serial2.println();
                delay(10);
                sumatoriaCorrecto=0;
                sumatoriaError=0;  
                break;
                }
      //default:
                    /*while(Serial2.available()==0)
                    {}
                    if(Serial2.read()==0)   //es velocidad 0
                      {
                      rutinaNotaOff(lectura);
                      }
                    else
                      {
                      rutinaNotaOn(lectura);  
                      }*/
      }
 }

void comandoOff()
  {
  while(Serial2.available()==0)
  {}
  rutinaNotaOff(Serial2.read());
  //while(Serial2.available()==0)
  //{}
  //velocidad=Serial2.read();
  }

void comandoOn()
{
  while(Serial2.available()==0)
  {}
  nota=Serial2.read();
  rutinaNotaOn(nota);
  /*while(Serial2.available()==0)
  {}
  velocidad=Serial2.read();
  if(velocidad==0)  //si w==1 es porque hay una posible nota off
    {
     rutinaNotaOff(nota);
     }
  else
    {
    rutinaNotaOn(nota);   
    }*/
    
}

void rutinaNotaOff (byte tono)
  {
  if(tono<=teclaFinal)
    {
     if(tono>=teclaInicial)
        {
          tecla=tono-teclaInicial;
          vectorNota[tecla]=0;
          notaMatriz[0][tecla]=0;
          if(notaMatriz[1][tecla]==0) //si esto sucede es porque hubo nota corta
            {
              notaMatriz[0][tecla]=1;
              vectorNotaCorta[tecla]=1;
            }
        }
    }
  }

 void rutinaNotaOn (byte tono)
 {

   if(tono<=teclaFinal)
    {
      if(tono>=teclaInicial)
        {
          tecla=tono-teclaInicial;
          if(notaMatriz[1][tecla]==1)
            {
              vectorNota[tecla]=1;  //si esto sucede, es porque hay una secuencia nota ON-OFF-ON muy rápida, entonces no prendemos el LED
            }
          else
            {
             notaMatriz[0][tecla]=1;
             
            }
          
        }
    } 
 }


//------------------------Piano-------------------------

void serialEvent3() 
  {
    lectura= Serial3.read();
    
    switch (lectura)
      {
      
      case (128+canalA):
                    comando1Off(); // hay notaOff
                    break;
      case (144+canalA):
                    comando1On(); //hay notaOn
                    break;               
      case (128+canalB):
                    comando1Off(); // hay notaOff
                    break;
      case (144+canalB):
                    comando1On();
                    break;
      case (128+canalC):
                    comando1Off(); // hay notaOff
                    break;
      case (144+canalC):
                    comando1On(); //hay notaOn
                    break;  
      case (128+canalD):
                    comando1Off(); // hay notaOff
                    break;
      case (144+canalD):
                    comando1On(); //hay notaOn
                    break;  
      case (128+canalE):
                    comando1Off(); // hay notaOff
                    break;
      case (144+canalE):
                    comando1On(); //hay notaOn
                    break;  
      case (128+canalF):
                    comando1Off(); // hay notaOff
                    break;
      case (144+canalF):
                    comando1On(); //hay notaOn
                    break;  
      case (128+canalG):
                    comando1Off(); // hay notaOff
                    break;
      case (144+canalG):
                    comando1On(); //hay notaOn
                    break;  
      case (128+canalH):
                    comando1Off(); // hay notaOff
                    break;
      case (144+canalH):
                    comando1On(); //hay notaOn
                    break;  
     case (128+canalI):
                    comando1Off(); // hay notaOff
                    break;
      case (144+canalI):
                    comando1On(); //hay notaOn
                    break;  
      case (128+canalJ):
                    comando1Off(); // hay notaOff
                    break;
      case (144+canalJ):
                    comando1On(); //hay notaOn
                    break;  
      case (128+canalK):
                    comando1Off(); // hay notaOff
                    break;
      case (144+canalK):
                    comando1On(); //hay notaOn
                    break;  
    case (128+canalL):
                    comando1Off(); // hay notaOff
                    break;
      case (144+canalL):
                    comando1On(); //hay notaOn
                    break;  
      case (128+canalM):
                    comando1Off(); // hay notaOff
                    break;
      case (144+canalM):
                    comando1On(); //hay notaOn
                    break;  
     case (128+canalN):
                    comando1Off(); // hay notaOff
                    break;
      case (144+canalN):
                    comando1On(); //hay notaOn
                    break;  
      case (128+canalO):
                    comando1Off(); // hay notaOff
                    break;
      case (144+canalO):
                    comando1On(); //hay notaOn
                    break;  
     case (128+canalP):
                    comando1Off(); // hay notaOff
                    break;
      case (144+canalP):
                    comando1On(); //hay notaOn
                    break;
      default:
                    while(Serial3.available()==0)
                    {}
                    if(Serial3.read()==0)   //es velocidad 0
                      {
                      comparacionOff(lectura);
                      }
                    else
                      {
                      comparacionOn(lectura);  
                      }             
  
      }
    }

void comando1Off()
  {
  while(Serial3.available()==0)
  {}
  comparacionOff(Serial3.read());
/*  while(Serial3.available()==0)
 {}
  velocidad=Serial3.read();
  */
  }

void comando1On()
{
  while(Serial3.available()==0)
  {}
  nota=Serial3.read();
  while(Serial3.available()==0)
  {}
  velocidad=Serial3.read();
  if(velocidad==0) 
    {
     comparacionOff(nota);
     }
  else
    {
    comparacionOn(nota);
    }  
}

void comparacionOn(byte temp)
  {
    if(temp<=teclaFinal)
    {
      if(temp>=teclaInicial)
        {
        tecla=temp-teclaInicial;
        vectorTeclaPresionada[tecla]=1;
        if((notaMatriz[14][tecla]||notaMatriz[15][tecla]||notaMatriz[16][tecla])==1)
          {
            sumatoriaCorrecto++;
          }
        else
          {
            sumatoriaError=sumatoriaError+1;  
          }
        }
    }
   
  }

void comparacionOff(byte temp)
  {
    if(temp<=teclaFinal)
    {
      if(temp>=teclaInicial)
        {
        tecla=temp-teclaInicial;
        vectorTeclaPresionada[tecla]=0;
        }
    }
   
  }

//--------------------------Puntaje---------------------------------------
void puntaje()  //tiempoCorrecto suma solomante cuando cuando hay notas presionadas y coinciden con lo mostrado en pantalla
                //si no se presiona ninguna tecla y la pantalla tampoco muestra ninguna nota(de la ultima fila), se detiene la suma del tiempoCorrecto o del tiempoError
                //siempre y cuando exista alguna de sus respectivas banderas activadas
                //siempre que haya una discrepancia entre las teclas presionadas y lo mostrado en pantalla, comienza a correr el tiempoError y se detiene el tiempoCorrecto
                //esto incluye el momomento en el cual se esta presionando una tecla y en la pantalla está todo apagado
    {   
    for(int i=0;i<60;i++)
      {
        if((notaMatriz[15][i]==vectorTeclaPresionada[i])||(notaMatriz[16][i]==vectorTeclaPresionada[i])||(notaMatriz[14][i]==vectorTeclaPresionada[i]))
          {
            distinto=0;
          }
        else
          {
            distinto=1;
            i=60;  
          }
      }

      boolean sinTocar=0;  
      for(int i=0;i<60;i++)
        {
          if(vectorTeclaPresionada[i]==0)
            {
              sinTocar=1;
            }
          else
            {
              sinTocar=0;
              i=60;  
            }
        }
      
    if(distinto==1)
      {   
      
       //if (sinTocar==1) //si no se presiona teclas y hay notas debería haber error
        //{
        sumatoriaError++;
        //}
      
      }
     else
      {
        if(sinTocar==0)
          {
            sumatoriaCorrecto++;
          }
         
      }

    }                  
