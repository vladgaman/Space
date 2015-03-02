/*
==========================================================================
File:        ex2.c (skeleton)
Authors:     Toby Howard
==========================================================================
*/

/* The following ratios are not to scale: */
/* Moon orbit : planet orbit */
/* Orbit radius : body radius */
/* Sun radius : planet radius */

#include <GL/glut.h>
#include <math.h>
#include <stdio.h>
#include <stdlib.h>
#include "frames.h"

#define MAX_STARS 1000
#define MAX_BODIES 20
#define TOP_VIEW 1
#define ECLIPTIC_VIEW 2
#define SHIP_VIEW 3
#define EARTH_VIEW 4
#define PI 3.14159
#define DEG_TO_RAD 0.017453293
#define ORBIT_POLY_SIDES 40
#define TIME_STEP 0.5   /* days per frame */
#define TURN_ANGLE 4.0
#define MOUSE_TURN_ANGLE 1.0
#define RUN_SPEED  1000000.0
#define LOOK_POINT_TURN_ANG 50000000

typedef struct 
 { 
  char    name[20];       /* name */
  GLfloat r, g, b;        /* colour */
  GLfloat orbital_radius; /* distance to parent body (km) */
  GLfloat orbital_tilt;   /* angle of orbit wrt ecliptic (deg) */
  GLfloat orbital_period; /* time taken to orbit (days) */
  GLfloat radius;         /* radius of body (km) */
  GLfloat axis_tilt;      /* tilt of axis wrt body's orbital plane (deg) */
  GLfloat rot_period;     /* body's period of rotation (days) */
  GLint   orbits_body;    /* identifier of parent body */
  GLfloat spin;           /* current spin value (deg) */
  GLfloat orbit;          /* current orbit value (deg) */
 } body;

typedef struct 
 {
   GLfloat x,y,z; // coordinates of a star
 } star;

/* Global declaration */
  // Misc stuff
  body  bodies[MAX_BODIES]; // bodies stored
  int   numBodies;          // number of bodies
  int   current_view;       // current view of camera
  int   draw_labels;        // the labels can be drawn or not
  int   draw_orbits;        // the orbits cab be drawn or not
  int   draw_starfield;     // the stars can be drawn or not
  int   redraw_stars;       // give new position for stars
  int   lightOn;            // light can be on or off
  int   wireFrame;          // models can be wired or solid

  // Used for lighting 
  GLfloat white_light[]     = { 1.0, 1.0, 1.0, 0.0 }; // default color of light
  GLfloat light_position0[] = { 0.0, 0.0, 0.0, 1.0 }; // the position is in the center of solar system
  GLfloat matSpecular[]     = { 0.0, 0.0, 0.0, 0.0 }; // the default material spec
  GLfloat matShininess[]    = { 50.0 };               // default shininess
  GLfloat matSurface[]      = { 1.0, 1.0, 1.0, 0.0 }; // default material surface

  // Used for stars
  star stars[MAX_STARS];  // and are stored in here

  // Used for time of solar system
  float date;     // current date of solar system (days)
  GLfloat timeO;  // slowing down or speeding up the time

  // Used for free view
  GLdouble lat,     lon;              // View angles (degrees)    
  GLdouble mlat,    mlon;             // Mouse look offset angles   
  GLfloat  eyex,    eyey,    eyez;    // Eye point                
  GLfloat  centerx, centery, centerz; // Look point               
  GLfloat  upx,     upy,     upz;     // View up vector           
  int height, width;

  // Used for eliptical view
  int   elipView;   // view along z or x axis

  // Used for viewing from the planets/moons
  GLfloat earthVx;        // position of camera on x 
  GLfloat earthVy;        // position of camera on y  
  GLfloat earthVz;        // position of camera on z
  GLfloat earthLx;        // looking point of camera on x
  GLfloat earthLy;        // looking point of camera on y
  GLfloat earthLz;        // looking point of camera on z
  int   body_view;        // from which body to observe
  int   body_la;          // at which body to look
  int   distToBody;       // the distance of camera from the body
  GLfloat   percentage;   // the percentage of distance from the body
/*****************************/

void calculate_lookpoint(void) 
 { 
    // Compute the looking point when moving the mouse
    // the first cosine is for y value of the looking point using the latitude, along the planes xy, zy
    // the second cosine of z and the sine of x  are for the looking point using the longitude, along the plane xz
    centerx = eyex - cos((lat+mlat)*DEG_TO_RAD)*sin((lon+mlon)*DEG_TO_RAD) * LOOK_POINT_TURN_ANG;
    centerz = eyez - cos((lat+mlat)*DEG_TO_RAD)*cos((lon+mlon)*DEG_TO_RAD) * LOOK_POINT_TURN_ANG;
    // this is for the y value of the looking point using the latitude, along the planes xy,zy
    centery = eyey + sin((lat+mlat)*DEG_TO_RAD)                            * LOOK_POINT_TURN_ANG;
    // redraw
    glutPostRedisplay();
 } // calculate_lookpoint()

/*****************************/

void mouse_motion(int x, int y) 
 {
  // there will be a limit for latitude, no longer than +/- 90 degrees
  if(lat+60*(height/2-y)/height < 90 && lat+60*(height/2-y)/height > -90)
  {
    // the mouse latitude will be got by refering to the middle of the window's height
    mlat = 60*(height/2-y)/height;
    // this is for turning the view more than the mouse can let
    if(y <= 10 && lat+mlat+MOUSE_TURN_ANGLE < 90)
    {
      lat += MOUSE_TURN_ANGLE;
    }
    if(y >= height - 10 && lat+mlat-MOUSE_TURN_ANGLE > -90)
    {
      lat -= MOUSE_TURN_ANGLE;
    }
  }
  // the mouse longitude will be got by refering to the middle of the window's width
  mlon = 60*(width/2-x)/width;

  // this is for turning the view more than the mouse can let
  if(x <= 10)
  {
    lon += MOUSE_TURN_ANGLE;
  }
  if(x >= width - 10)
  {
    lon -= MOUSE_TURN_ANGLE;
  }
  glutPostRedisplay();
 } // mouse_motion()

/*****************************/

float myRand (void)
 {
  /* return a random float in the range [0,1] */
  return (float) rand() / RAND_MAX;
 }

/*****************************/

void drawStarfield (void)
 {
  // if the light is on, then it is no use on the stars
  if(lightOn)
  {
    glDisable(GL_LIGHTING);
  }
  int i;
  // redraw after it is re-enabled
  if(redraw_stars)
  {
    int max = 300000000;
    // set the position of each star
    for(i=0; i<MAX_STARS;i++)
    {
        stars[i].x = myRand() * 2 * max - max;
        stars[i].y = myRand() * 2 * max - max;
        stars[i].z = myRand() * 2 * max - max;
    }
    redraw_stars = 0;
  }
  // start drawing points
  glBegin (GL_POINTS);
    for(i=0; i<MAX_STARS;i++)
    {
      // make them white
      glColor3f(1.0,1.0,1.0);
      glVertex3f(stars[i].x,stars[i].y,stars[i].z);
    }
  glEnd();
  // turn the light back on
  if(lightOn)
  {
    glEnable(GL_LIGHTING);
  }
 }

/*****************************/

void readSystem(void)
 {
  /* reads in the description of the solar system */

  FILE *f;
  int i;

  f= fopen("sys", "r");
  if (f == NULL) {
     printf("ex2.c: Couldn't open the datafile 'sys'\n");
     printf("To get this file, use the following command:\n");
     printf("  cp /opt/info/courses/COMP27112/ex2/sys .\n");
     exit(0);
  }
  fscanf(f, "%d", &numBodies);
  for (i= 0; i < numBodies; i++)
  {
    fscanf(f, "%s %f %f %f %f %f %f %f %f %f %d", 
      bodies[i].name,
      &bodies[i].r, &bodies[i].g, &bodies[i].b,
      &bodies[i].orbital_radius,
      &bodies[i].orbital_tilt,
      &bodies[i].orbital_period,
      &bodies[i].radius,
      &bodies[i].axis_tilt,
      &bodies[i].rot_period,
      &bodies[i].orbits_body);

    /* Initialise the body's state */
    bodies[i].spin= 0.0;
    bodies[i].orbit= myRand() * 360.0; /* Start each body's orbit at a
                                          random angle */
    bodies[i].radius*= 1000.0; /* Magnify the radii to make them visible */
  }
  fclose(f);
 }

/*****************************/

void drawString (void *font, float x, float y, char *str)
 { /* Displays the string "str" at (x,y,0), using font "font" */
  char *ch;
  glRasterPos2f(x,y); // draw string on these axes
  for (ch= str; *ch; ch++)
      glutBitmapCharacter(font, *ch); // draw each character with the specified font
 }

/*****************************/

void showDate(void)
 { /* Displays the current date */
  frameEnd(GLUT_BITMAP_HELVETICA_18,1.0,1.0,1.0,0.01,0.01,date);
 }

 /*****************************/

void posToMoon(int n)
 {
  // if body's parent is not the sun than do some calculations, until the body's parent is the sun
  if(bodies[n].orbits_body != 0)
  {
    // get the parent
    GLint par = bodies[n].orbits_body;
    // the y position of camera is computed by the body's parent's orbital tilt  
    earthVy += sin(-DEG_TO_RAD*(bodies[par].orbit + 90))*bodies[par].orbital_radius * sin(DEG_TO_RAD*bodies[par].orbital_tilt);
    // the x and z position of the camera is computed by the orbital radius of the parent and this body's radius
    earthVx += (bodies[par].orbital_radius + bodies[n].radius)*sin(DEG_TO_RAD*bodies[par].orbit);
    earthVz += (bodies[par].orbital_radius + bodies[n].radius)*cos(DEG_TO_RAD*bodies[par].orbit);
    posToMoon(par);
  }
 }

/*****************************/

void poToBody(int n)
 {

  distToBody = bodies[n].radius * percentage;
  // the parent is the sun, then do this simpler computation
  if(bodies[n].orbits_body == 0)
  {
    earthVy = bodies[n].radius + sin(-DEG_TO_RAD*(bodies[n].orbit + 90))*bodies[n].orbital_radius * sin(DEG_TO_RAD*bodies[n].orbital_tilt);

    earthVx = (bodies[n].orbital_radius + distToBody)*sin(DEG_TO_RAD*bodies[n].orbit);
    earthVz = (bodies[n].orbital_radius + distToBody)*cos(DEG_TO_RAD*bodies[n].orbit);

    earthLx = earthLz = 0.0;
    earthLy = sin(-DEG_TO_RAD*(bodies[n].orbit + 90))*bodies[n].orbital_radius * sin(DEG_TO_RAD*bodies[n].orbital_tilt);
  }
  else
  {
    if(!body_la)
    {
      earthLx = earthLy = earthLz = 0.0;
      GLint par = bodies[n].orbits_body;

      earthVy = bodies[n].radius + sin(-DEG_TO_RAD*(bodies[n].orbit + 90))*bodies[n].orbital_radius * sin(DEG_TO_RAD*bodies[par].orbital_tilt);
      earthVx = (bodies[n].orbital_radius )*sin(DEG_TO_RAD*bodies[n].orbit);
      earthVz = (bodies[n].orbital_radius )*cos(DEG_TO_RAD*bodies[n].orbit);
      posToMoon(n);
    }
    else
    {
      GLint par = bodies[n].orbits_body;
      earthLx = (bodies[par].orbital_radius)*sin(DEG_TO_RAD*bodies[par].orbit);
      earthLy = sin(-DEG_TO_RAD*(bodies[par].orbit + 90))*bodies[par].orbital_radius * sin(DEG_TO_RAD*bodies[par].orbital_tilt);
      earthLz = (bodies[par].orbital_radius)*cos(DEG_TO_RAD*bodies[par].orbit);

      earthVy = sin(-DEG_TO_RAD*(bodies[par].orbit + 90))*bodies[par].orbital_radius * sin(DEG_TO_RAD*bodies[par].orbital_tilt);
      earthVx = (bodies[par].orbital_radius)*sin(DEG_TO_RAD*bodies[par].orbit);
      earthVz = (bodies[par].orbital_radius)*cos(DEG_TO_RAD*bodies[par].orbit);

      earthVy += bodies[n].radius + sin(-DEG_TO_RAD*(bodies[n].orbit + 90))*bodies[n].orbital_radius * sin(DEG_TO_RAD*bodies[par].orbital_tilt);
      earthVx += (bodies[n].orbital_radius + bodies[n].radius)*sin(DEG_TO_RAD*bodies[n].orbit);
      earthVz += (bodies[n].orbital_radius + bodies[n].radius)*cos(DEG_TO_RAD*bodies[n].orbit);
    }
  }
 }

/*****************************/

void setView (void) 
 {
  glMatrixMode(GL_MODELVIEW);
  // if light is on, then position it in the world
  if(lightOn)
  {
    glLightfv(GL_LIGHT0, GL_POSITION, light_position0);
  }
  glLoadIdentity();
  // camera distance when viewing along each axis
  float camDist = 500000000.0;
  switch (current_view) 
  {
    case TOP_VIEW: // look along y axis
      gluLookAt(0.0,camDist,0.0, 0.0,0.0,0.0, 0.0,0.0,-1.0);
      break;  
    case ECLIPTIC_VIEW:
      // choose to view along x or z axis
      if(elipView)
      {
        // view along the z axis
        gluLookAt(0.0,0.0,camDist,0.0,0.0,0.0,0.0,1.0,0.0);
      }
      else
      {
        // view along the x axis
        gluLookAt(camDist,0.0,0.0,0.0,0.0,0.0,0.0,1.0,0.0);
      }
      break;  
    case SHIP_VIEW:
      // free view in the solar system
      gluLookAt(eyex,eyey,eyez,centerx,centery,centerz,upx,upy,upz);   
      break;  
    case EARTH_VIEW: 
      // view from the "shoulder" of each body (except the sun), to sun or it's parent 
      poToBody(body_view);
      gluLookAt(earthVx,earthVy,earthVz,earthLx,earthLy,earthLz,0.0,1.0,0.0);
      break;  
  }
 }

/*****************************/

void menu (int menuentry) 
 {
  switch (menuentry) {
  case 1: current_view= TOP_VIEW;
          break;
  case 2: current_view= ECLIPTIC_VIEW;
          break;
  case 3: current_view= SHIP_VIEW;
          break;
  case 4: current_view= EARTH_VIEW;
          break;
  case 5: draw_labels= !draw_labels;
          break;
  case 6: draw_orbits= !draw_orbits;
          break;
  case 7: draw_starfield= !draw_starfield;
          redraw_stars = 1;
          break;
  case 8: exit(0);
  }
 }

/*****************************/

void init(void)
 {
  /* Define background colour */
  glClearColor(0.0, 0.0, 0.0, 0.0);

  glutCreateMenu (menu);
  glutAddMenuEntry ("Top view", 1);
  glutAddMenuEntry ("Ecliptic view", 2);
  glutAddMenuEntry ("Spaceship view", 3);
  glutAddMenuEntry ("Earth view", 4);
  glutAddMenuEntry ("", 999);
  glutAddMenuEntry ("Toggle labels", 5);
  glutAddMenuEntry ("Toggle orbits", 6);
  glutAddMenuEntry ("Toggle starfield", 7);
  glutAddMenuEntry ("", 999);
  glutAddMenuEntry ("Quit", 8);
  glutAttachMenu (GLUT_RIGHT_BUTTON);

  glLightfv(GL_LIGHT0, GL_DIFFUSE, white_light);  // set the diffuse light
  glLightfv(GL_LIGHT0, GL_SPECULAR, white_light); // set the specular light
  glEnable(GL_DEPTH_TEST);                        // enable the depth buffer
  glShadeModel(GL_SMOOTH);                        // smooth out the models
  lightOn = 0;                                    // light is not on

  draw_labels= 1;     // set to draw labels
  draw_orbits= 1;     // set to draw orbits
  draw_starfield= 1;  // set to draw stars
  redraw_stars = 1;   // set to redraw stars
  percentage = 1.02;  // set the percentage for distance of camera
  timeO = TIME_STEP;  // set time orbits to TIME_STEP
  wireFrame = 1;      // set to view the bodies as wire frames
  
  current_view= TOP_VIEW;   // current view is top view
  elipView = 0;             // set eliptice view to first view along x axis
  body_view = 3;            // initialy view from earth
  body_la = 0;              // look at sun
  eyex=  100000000.0;       // set the eye points of spaceship camera
  eyey=  100000000.0;
  eyez=  100000000.0;
  upx= 0.0;                 // set the up vectors of spaceship camera
  upy= 1.0;
  upz= 0.0;
  lat= -45.0;               // look at sun from above (look down at sun) 
  lon= 45.0;                // look between axes (at sun) 
  mlat= 0.0;                // set mouse look at zero
  mlon= 0.0;                    
  width = glutGet(GLUT_WINDOW_WIDTH);   // get width of window
  height = glutGet(GLUT_WINDOW_HEIGHT); // get height of window
 }

/*****************************/

void animate(void)
 {
  int i;
    
    date+= timeO;

    for (i= 0; i < numBodies; i++)  
    {
      bodies[i].spin += 360.0 * timeO / bodies[i].rot_period;
      bodies[i].orbit += 360.0 * timeO / bodies[i].orbital_period;
      glutPostRedisplay();
    }
 }

/*****************************/

void drawOrbit (int n)
 { 
    /* Draws a polygon to approximate the circular
     orbit of body "n" */
    int i;
    glColor3f(bodies[n].r,bodies[n].g,bodies[n].b);
    glBegin(GL_LINE_LOOP);
    // the angle between two points
    float pointsDeg = 360 / ORBIT_POLY_SIDES;
    // draw the points with a line between them
      for(i=0;i<ORBIT_POLY_SIDES;i++)
      {
        glVertex3f(sin(pointsDeg * i*DEG_TO_RAD)*bodies[n].orbital_radius, 0.0,\
                   cos(pointsDeg * i*DEG_TO_RAD)*bodies[n].orbital_radius);
      }
    glEnd();
 }

/*****************************/

void drawLabel(int n)
 { 
  /* Draws the name of body "n" */
  glRotatef(90 ,1.0,0.0,0.0);
  drawString(GLUT_BITMAP_HELVETICA_18,0,- bodies[n].radius * 3 / 2,bodies[n].name);
  glRotatef(-90 ,1.0,0.0,0.0);
 }

/*****************************/

void drawMoon(int n)
 {
  //is the parent is not the sun apply transformations
  if(bodies[n].orbits_body != 0)
  {
    // get parent of this body
    GLint par = bodies[n].orbits_body;
    // rotate the body with parent's orbital tilt
    glRotatef(bodies[par].orbital_tilt ,1.0,0.0,0.0);
    // translate the body with parent's radius and orbit
    glTranslatef(bodies[par].orbital_radius*sin(DEG_TO_RAD*bodies[par].orbit),0.0\
              ,bodies[par].orbital_radius*cos(DEG_TO_RAD*bodies[par].orbit));

    // recursively do this for moons of this moon
    drawMoon(par);
  }
 }

/*****************************/

void drawBody(int n)
 {
  // if light is on enable lighting
  if(lightOn)
  {
    // set light with sun's color
    GLfloat sun_light[] =  {bodies[0].r,bodies[0].g, bodies[0].b, 0.0 };
    glLightfv(GL_LIGHT0, GL_DIFFUSE, sun_light);
    glLightfv(GL_LIGHT0, GL_SPECULAR, sun_light);
    glEnable(GL_LIGHTING);
    glEnable(GL_LIGHT0);
    // set material of body with body's color
    GLfloat matSurface[] = {bodies[n].r,bodies[n].g,bodies[n].b,0.0};
    glMaterialfv(GL_FRONT, GL_AMBIENT,   matSurface);
  }
  // otherwise disable lighting
  else
  {
    glDisable(GL_LIGHTING);
    // set body's color
    glColor3f(bodies[n].r,bodies[n].g,bodies[n].b);
  }

  drawMoon(n);

  // if light is on disable it while drawing the orbits, labels and axis
  if(lightOn)
  {
    glDisable(GL_LIGHTING);
  }

  GLint par = bodies[n].orbits_body;
  if(par != 0)
  {
    glRotatef(-bodies[par].orbital_tilt  ,1.0,0.0,0.0);
  }
  // apply orbital tilt
  glRotatef(bodies[n].orbital_tilt ,1.0,0.0,0.0);
  // draw the orbit
  if(draw_orbits)
  {
    drawOrbit(n);
  }
  // apply the translation to nex position of body
  glTranslatef(bodies[n].orbital_radius*sin(DEG_TO_RAD*bodies[n].orbit),0.0\
              ,bodies[n].orbital_radius*cos(DEG_TO_RAD*bodies[n].orbit));
  // apply axis tilt
  glRotatef(bodies[n].axis_tilt ,1.0,0.0,0.0);
  // apply the spin 
  glRotatef(bodies[n].spin,0.0,1.0,0.0);
  // rotate the body 90 degrees, because OpenGL doesn't draw the spheres properly
  glRotatef(90 ,1.0,0.0,0.0);

  // draw the axis
  glBegin(GL_LINES);
    glVertex3f(0.0, 0.0, -3 * bodies[n].radius / 2);
    glVertex3f(0.0, 0.0, 3 * bodies[n].radius/2);
  glLineWidth(200.0);
  glEnd();
  

  if(draw_labels)
  {
    drawLabel(n);
  }

  // if light is on enable it back after done drawing the orbits, labels and axis
  if(lightOn)
  {
    glEnable(GL_LIGHTING);
  }

  // if this body is sun don't apply lighting
  if(n == 0 && lightOn)
  {
    glDisable(GL_LIGHTING);
  }
  if(wireFrame && !lightOn)
    glutWireSphere(bodies[n].radius,50,50);
  else
    glutSolidSphere(bodies[n].radius,50,50);
  // turn back the lighting after the sun
  if(n == 0 && lightOn)
  {
    glEnable(GL_LIGHTING);
  }
 }
/*****************************/

void drawAxes (void) 
 {

  // Draws X Y and Z axis lines, of length LEN

   float LEN= 10000000.0;

   glLineWidth(5.0);

   glBegin(GL_LINES);
   glColor3f(1.0,0.0,0.0); // red
       glVertex3f(0.0, 0.0, 0.0);
       glVertex3f(LEN, 0.0, 0.0);

   glColor3f(0.0,1.0,0.0); // green
       glVertex3f(0.0, 0.0, 0.0);
       glVertex3f(0.0, LEN, 0.0);

   glColor3f(0.0,0.0,1.0); // blue
       glVertex3f(0.0, 0.0, 0.0);
       glVertex3f(0.0, 0.0, LEN);
   glEnd();

   glLineWidth(1.0);
 }

/*****************************/

void display(void)
 {
  // clear the screen
  glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
  calculate_lookpoint();
  // set the view
  setView();
  // draw the stars
  if (draw_starfield)
    drawStarfield();
  // set the material shininess and specular
  glMaterialfv(GL_FRONT, GL_SPECULAR,  matSpecular);
  glMaterialfv(GL_FRONT, GL_SHININESS, matShininess);
  // draw each body
  int i;
  for (i= 0; i < numBodies; i++)
  {
    glPushMatrix();
      drawBody (i);
    glPopMatrix();
  }
  // show the date
  showDate();
  glutSwapBuffers();
 }

/*****************************/

void reshape(int w, int h)
 {
  glViewport(0, 0, (GLsizei) w, (GLsizei) h);
  glMatrixMode(GL_PROJECTION);
  glLoadIdentity();
  gluPerspective (48.0, (GLfloat) w/(GLfloat) h, 10000.0, 800000000.0);
 }
  
/*****************************/

void keyboard(unsigned char key, int x, int y)
 {
  switch (key)
  {
    case 27:  /* Escape key */
      exit(0);
    case 'l': lightOn = !lightOn;     // toggle the light
              break;
    case 'w': wireFrame = !wireFrame; // toggle the models
              break;
    case ']': elipView = 0; break;    // switch to view along x axis
    case '[': elipView = 1; break;    // switch to view along z axis
    case '.': timeO += 0.01; break;   // slow down the time
    case ',': timeO -= 0.01; break;   // speed up the time
    glutPostRedisplay();
  }
  if(current_view == EARTH_VIEW)     // in case is the earth view
  {
    switch (key)
    {
      case '=': percentage += 0.01; // go further away from the planet
                break;
      case '-': percentage -= 0.01; // get closer to the planet
                break;
    }
  }
 } 

/*****************************/

void cursor_keys(int key, int x, int y) 
 {
  // if it is the ship's view
  if(current_view == SHIP_VIEW)
  {
    switch (key) 
    {     
          // set the latitude to 0
          case GLUT_KEY_HOME: lat = 0; 
                              break;
          // go closer to the looking point
          case GLUT_KEY_UP: eyex-=sin((mlon+lon)*DEG_TO_RAD)*RUN_SPEED;
                            eyez-=cos((mlon+lon)*DEG_TO_RAD)*RUN_SPEED;
                            eyey+=sin((mlat+lat)*DEG_TO_RAD)*RUN_SPEED; 
                            break;
          //go further then the looking point
          case GLUT_KEY_DOWN: eyex+=sin((mlon+lon)*DEG_TO_RAD)*RUN_SPEED;
                              eyez+=cos((mlon+lon)*DEG_TO_RAD)*RUN_SPEED;
                              eyey-=sin((mlat+lat)*DEG_TO_RAD)*RUN_SPEED;
                              break;
          // strafe left
          case GLUT_KEY_LEFT: eyex-=sin((90+lon+mlon)*DEG_TO_RAD)*RUN_SPEED;
                              eyez-=cos((90+lon+mlon)*DEG_TO_RAD)*RUN_SPEED;
                              break;
          // strafe right
          case GLUT_KEY_RIGHT:  eyex+=sin((90+lon+mlon)*DEG_TO_RAD)*RUN_SPEED;
                                eyez+=cos((90+lon+mlon)*DEG_TO_RAD)*RUN_SPEED;
                                break;
    }
  }
  // if the view is on earth
  else if(current_view == EARTH_VIEW)
  {
    switch (key) 
    {
          // change the view to next body
          case GLUT_KEY_RIGHT:  body_view += 1;
                                body_view = body_view % numBodies; 
                                if(body_view == 0)
                                  body_view = 1;
                                break;
          // change the view to previous body
          case GLUT_KEY_LEFT: body_view -= 1;
                              if(body_view <= 0)
                                body_view = 7;
                              break;
          // change to look at the sun
          case GLUT_KEY_UP:   body_la = 0;
                              break;
          // change to look at this body's parent
          case GLUT_KEY_DOWN: body_la = 1;
                              break;

    }
  }
  glutPostRedisplay();
 } // cursor_keys()

/*****************************/

int main(int argc, char** argv)
 {
  glutInit (&argc, argv);
  glutInitDisplayMode (GLUT_DOUBLE | GLUT_RGB | GLUT_DEPTH);
  glutCreateWindow ("COMP27112 Exercise 2");
  glutFullScreen();
  init ();
  glutDisplayFunc (display); 
  glutReshapeFunc (reshape);
  glutKeyboardFunc (keyboard);
  glutSpecialFunc (cursor_keys);
  glutIdleFunc (animate);
  glutPassiveMotionFunc (mouse_motion);
  readSystem();
  glutMainLoop ();
  return 0;
 }
/* end of ex2.c */
