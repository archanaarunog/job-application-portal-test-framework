## Test case 1 - Meta Portal main page

**Steps** 
1. Navigate to site http://127.0.0.1:8000/index.html , ensure page loads with images as 
2. Ensure on top there is Features , How it works, rowse Jobs followed by Login and get started present are clickable 
3. Ensure the text s displayed signed as per design plan 
4. Validate get started and browse jobs button clickable 
5. Verify able to scroll down to display Why choose meta portal page 
6. Verify able to scroll further to display How it works design 
7. Verify able to scroll further to display Ready to start your journey 
8. Verify create free account and sign button clickable 
9. Verify footer has Meta portal details  , quick links, company, support, For employee options visible 
10. Scroll on top and click on Features - verify it scroll to why chose meta page view
11. Verify on placing mouse over the four cards, it is highlighter with shadow 
12. Scroll on top and click on How it works  - verify it scroll to how it works page view
11. Verify design matches
14. Click on browser Jobs - ensure shows the jobs portal page with job listing default page 
15. Navigate back to index page, click on login button - ensure login page displayed
16. Navigate back and click on Get started button - ensure takes to create account page 
17. Navigate back and click on Get started free - ensure takes you to create login page 
18. Navigate back and click on browser jobs page - ensure takes you to job portal listing page 
19. Scroll down and click on create account - ensure takes to create account 
20. Navigate back and click on Login - takes to login page 
21. Click on the Facebook, witter, isnta , linked icons - check each opens (currently feature not done)
22. In Quick links , each home, browser jobs, sign up, login works 
23. Similarly for company, support, or employee (only placeholders , mark as need to implement )

## Test case 2 - Create account page 
1. Verify design matches as per plan - contains 6 fields 
2. Click on terms of service - ensure terms displayed 
3. Click on privacy policy - ensure it is displayed pop up pens and displays 
4. Click on sign in - ensure goes to login page 
5. Click on Sign up now - ensure goes back to Create account page 
6. Click on back to home page - verify goes back to home page
7. Enter details as follows, Amekha J , sampleTest@gmail.com , 1122334455, Password@123, 
8. Verify the password while being typed all the fields turn to green indicating strong password 
9. Confirm password with same green check marks come 
10. Verify create account is disabled until terms of service is checked 
11. Click create account after clicking terms 
12. Ensure created, login … text 

## Create account  additional cases 
Create account  page - validate each field first name, last name min 2 characters , mail - valid email, phone 10 digit phone number, password, strong and matching - test cases too 
Verify not Abel to create with registered email again 

## Login page - 
1. Enter ceased valid credentials - test123@gmail.com , Password@123 - ensure directs to job portal listing page 
2. Logout and Verify invalid credentials  (observing bug in signing page reloads)
3. Click on forget pass word - reset password email - need to check code base implementation 
4. Remember me for 30 days - functionality check code base and create test case
5. Click back to home page - verify goes back to main page 

## Candidate Job listing -header page 
1. Header click on workday portal - verify takes to main page 
2. Click on browser jobs - verify displays jobs page 
3. Click on my dashboard - check welcome back
4. Click on resume - currently bug - need to update 
5. Click on Profile - ensure profile page displayed 
6. Click on logos - verify logs out
7. One more header Meta portal observed with Home and logout - two logouts present - raise as bug to redesign 

## Candidate Job portal 
1. Verify 4 cards 
2. Validate 4 cards updated according to action 
3. Saved jobs not updating correctly - raise bug 

## Filters - high level features
1. Search Job - observed on licking on search bar applicants count keep changing 
2. Location based 
3. Job type filter 
4. Experience level - not working 
5. Salary range 
6. Clear filters

## Job card features 
1. View details - info present 
2. Close and open again 
3. Apply button 
4. Apply button pop up 
5. Application submitted successfully - pop up needs to be there
6. Applied confirmation
7. Srt by - not working - raise bug 
8. Load more jobs - , showing all those texts 

## My application
1. 4 cards updating 
2. Application history 
3. Refresh button 
4. Actions button - not yet implemented 
5. Check able to navigate back to others page from current page

## My dashboard 
1. Welcome back card
2. Filter by status 6 filters and sort by 
3. Applied job cards details - view job bug - should show details of Job not direct to job page 
4. Check able to navigate back to others page from current page

## Profile 
1 .basic details - name, number of applications 
2. Personal information
3. Professional information 
4. Links - all information 
5. Close button 
6. Submit button 
7. In profile section, resume header not being displayed - bug 

## Resume section - bug 

## Logout page - basic checks 

## corner cases/ edge cases
1. empty inputs for logn /credentails page
2. maximum characters 
update till 10 test cases 

### pending
1. admin page all layouts 



