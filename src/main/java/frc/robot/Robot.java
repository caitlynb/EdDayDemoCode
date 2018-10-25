/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot;

//import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.I2C;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.RobotController;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.CameraServer;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.can.*;


/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the TimedRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the build.properties file in the
 * project.
 */
public class Robot extends TimedRobot {
	private static final String kDefaultAuto = "Default";
	private static final String kCustomAuto = "My Auto";
	private String m_autoSelected;
	private SendableChooser<String> m_chooser = new SendableChooser<>();
	
	
	// Motor Controllers
	private WPI_TalonSRX leftmc = new WPI_TalonSRX(20);
  private WPI_TalonSRX rightmc = new WPI_TalonSRX(21);
  private WPI_TalonSRX demomc = new WPI_TalonSRX(23);
	private boolean motorsenabled = false;

	// Define Drive Train
	private DifferentialDrive drivetrain = new DifferentialDrive(leftmc, rightmc);

	// Define user controls
	private Joystick driverJoystick = new Joystick(0);
	
	// Arduino I2C Stuff
	private I2C I2CArduino = new I2C(I2C.Port.kOnboard, 0x08);
	private byte arduinoData[] = new byte[3];
	//private byte arduinoReceive[] = new byte[3];
	
	// Battery monitor stuff
	private double curBatteryVoltage;
	private static final double battWarnVoltage =  11.1;
	private static final double battCritVoltage = 10.5;
  private static final double battESTOPVoltage = 9.9;
  
  public double targetrpm = 200;

	
	
	/**
	 * This function is run when the robot is first started up and should be
	 * used for any initialization code.
	 */
	@Override
	public void robotInit() {
		//m_chooser.addDefault("Default Auto", kDefaultAuto);
		//m_chooser.addObject("My Auto", kCustomAuto);
		//SmartDashboard.putData("Auto choices", m_chooser);
    //SmartDashboard.putNumber("I am a number", 3.2);

    // easy peasy one line USB camera for driver view
    CameraServer.getInstance().startAutomaticCapture();
		
		// Setup some safety limits
		leftmc.configPeakCurrentDuration(500,10);
		leftmc.configPeakCurrentLimit(50,10);
		leftmc.configContinuousCurrentLimit(25,10);
		rightmc.configPeakCurrentDuration(500,10);
		rightmc.configPeakCurrentLimit(50,10);
    rightmc.configContinuousCurrentLimit(25,10);
    
    demomc.configPeakCurrentDuration(500, 10);
    demomc.configPeakCurrentLimit(10, 10);
    demomc.configContinuousCurrentLimit(5, 10);
		
		leftmc.enableCurrentLimit(true);
    rightmc.enableCurrentLimit(true);
    demomc.enableCurrentLimit(true);
    
    SmartDashboard.putNumber("TargetRPM", targetrpm);

		arduinoData[0] = 0;
		arduinoData[1] = 0;
    arduinoData[2] = 0;
    
    // Set up the Talon SRX for velocity control
    demomc.configSelectedFeedbackSensor(FeedbackDevice.CTRE_MagEncoder_Relative, 0, 10);
    demomc.setSensorPhase(false);
    demomc.configNominalOutputForward(0, 10);
    demomc.configNominalOutputReverse(0, 10);
    demomc.configPeakOutputForward(1, 10);
    demomc.configPeakOutputReverse(-1, 10);
    demomc.config_kF(0, 0.4, 10);
    demomc.config_kP(0, 0.3, 10);
    demomc.config_kI(0, 0, 10);
    demomc.config_kD(0, 0, 10);
	demomc.setNeutralMode(NeutralMode.Coast);
	demomc.setInverted(false);

	}

	/**
	 * This autonomous (along with the chooser code above) shows how to select
	 * between different autonomous modes using the dashboard. The sendable
	 * chooser code works with the Java SmartDashboard. If you prefer the
	 * LabVIEW Dashboard, remove all of the chooser code and uncomment the
	 * getString line to get the auto name from the text box below the Gyro
	 *
	 * <p>You can add additional auto modes by adding additional comparisons to
	 * the switch structure below with additional strings. If using the
	 * SendableChooser make sure to add them to the chooser code above as well.
	 */
	@Override
	public void autonomousInit() {
		m_autoSelected = m_chooser.getSelected();
		// autoSelected = SmartDashboard.getString("Auto Selector",
		// defaultAuto);
		System.out.println("Auto selected: " + m_autoSelected);
		
		arduinoData[0] = 0x02;
		I2CArduino.writeBulk(arduinoData, 3);
    SmartDashboard.putRaw("Arduino I2C Command", arduinoData);
	}

	/**
	 * This function is called periodically during autonomous.
	 */
	@Override
	public void autonomousPeriodic() {
		switch (m_autoSelected) {
			case kCustomAuto:
				if(motorsenabled){
					drivetrain.arcadeDrive(0, 0);
				} else {
					drivetrain.arcadeDrive(0, 0);
				}				
				break;
			case kDefaultAuto:
			default:
        // Put default auto code here

        motorsenabled = false; // EdDay2018 Specific!!!!
				if(motorsenabled){
					drivetrain.arcadeDrive(0, 0);
				} 
				break;
		}
		
		
		arduinoData[0] = 0x02;
		I2CArduino.writeBulk(arduinoData, 3);
	}
	
	/**
	 * This function is called when entering teleop mode
	 */
	@Override
	public void teleopInit(){
		arduinoData[0] = 0x04;
		I2CArduino.writeBulk(arduinoData, 3);
    SmartDashboard.putRaw("Arduino I2C Command", arduinoData);
    
	}

	/**
	 * This function is called periodically during operator control.
	 */
	@Override
	public void teleopPeriodic() {
		double speed = -driverJoystick.getRawAxis(1);
    double turn = -driverJoystick.getRawAxis(2);
    
    motorsenabled = false; // EdDay2018 Specific!!!!
		if(motorsenabled){
			drivetrain.arcadeDrive(speed, turn);
		} else {
			drivetrain.arcadeDrive(0, 0);
    }
    
    targetrpm = SmartDashboard.getNumber("TargetRPM", 200);

    SmartDashboard.putNumber("Motor Output Percent", demomc.getMotorOutputPercent());
    SmartDashboard.putNumber("MC Sensor Reading", demomc.getSelectedSensorVelocity(0));

    if(driverJoystick.getRawButton(2)){
      double leftystick = driverJoystick.getY();
      double motorOutput = demomc.getMotorOutputPercent();
      double targetVelocity_UnitsPer100ms = leftystick * 4096 * targetrpm / 600;
      demomc.set(ControlMode.Velocity, targetVelocity_UnitsPer100ms);

      SmartDashboard.putNumber("Closed Loop Error", demomc.getClosedLoopError(0));

    } else {
      demomc.set(0);
    }
		
		arduinoData[0] = 0x04;
		I2CArduino.writeBulk(arduinoData, 3);
//		I2CArduino.transaction(arduinoData, 3, arduinoReceive, 0);
		SmartDashboard.putRaw("Arduino I2C Command", arduinoData);

	}

	/**
	 * This function is called on Test mode init
	 */
	@Override
	public void testInit(){
		// update Shuffleboard
		arduinoData[0] = 0x08;
		I2CArduino.writeBulk(arduinoData, 3);
		SmartDashboard.putRaw("Arduino I2C Command", arduinoData);
	}
	
	/**
	 * This function is called periodically during test mode.
	 */
	@Override
	public void testPeriodic() {
		arduinoData[0] = 0x08;
		I2CArduino.writeBulk(arduinoData, 3);
		SmartDashboard.putRaw("Arduino I2C Command", arduinoData);

	}
	
	/**
	 * This function is called to init disabled
	 */
	@Override
	public void disabledInit(){
		arduinoData[0] = 0x01;
		I2CArduino.writeBulk(arduinoData, 3);
		SmartDashboard.putRaw("Arduino I2C Command", arduinoData);
	}
	
	/**
	 * This function is called periodically during disabled.
	 */
	@Override
	public void disabledPeriodic(){
		// watch battery voltage.
		arduinoData[0] = 0x01;
		I2CArduino.writeBulk(arduinoData, 3);
		SmartDashboard.putRaw("Arduino I2C Command", arduinoData);

	}
	


	
	/**
	 * This function is called on every driver station packet, regardless of mode
	 * 
	 */
	@Override 
	public void robotPeriodic(){
		// do nothing here
		// defer to our auto, teleop, test and disabled.
		curBatteryVoltage = RobotController.getBatteryVoltage();
		if(curBatteryVoltage > battWarnVoltage){
			leftmc.setNeutralMode(NeutralMode.Brake);
			rightmc.setNeutralMode(NeutralMode.Brake);
			arduinoData[1] = 0x01;
			motorsenabled = true;
		} else if(curBatteryVoltage > battCritVoltage){
			leftmc.setNeutralMode(NeutralMode.Brake);
			rightmc.setNeutralMode(NeutralMode.Brake);
			arduinoData[1] = 0x02;
			motorsenabled = true;
		} else if(curBatteryVoltage > battESTOPVoltage){
			leftmc.setNeutralMode(NeutralMode.Brake);
			rightmc.setNeutralMode(NeutralMode.Brake);
			arduinoData[1] = 0x04;
			motorsenabled = true;
		} else {
			arduinoData[1] = 0x08;
			leftmc.setNeutralMode(NeutralMode.Coast);
			rightmc.setNeutralMode(NeutralMode.Coast);
			motorsenabled = false;
		}
	}
}
