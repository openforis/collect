const routes = [
  {
    path: '/',
    name: 'Home'
  },
  {
    path: '/signin',
    name: "Signin"
  },
  {
    path: '/dashboard',
    name: "Dashboard"
  },
  {
    path: '/datamanagement',
    name: "Data Management"
  },
  {
    path: '/surveydesigner',
    name: "Survey Designer"
  },
  {
    path: '/datacleansing',
    name: "Data Cleansing"
  },
  {
    path: '/map',
    name: "Map"
  },
  {
    path: '/users',
    name: "Users"
  },
  {
    path: '/usergroups',
    name: "User Groups"
  },
  {
    path: '/usergroups/:id',
    name: 'User Group',
    pathRegExp: new RegExp('/usergroups/(\\d)+')
  },
  {
    path: '/usergroups/new',
    name: 'New User Group'
  }
];
export default routes;
