# Hydration: Daily Hydration Tracking
An android application focused on personalised hydration tracking and visualisation. See [progress](#progress) for planned features

## V2 Rewrite:

This project is currently undergoing a rewrite.  
The problem (V1): Rigid drink categories limited the user. If a user wanted to log a drink not defined in their four drinks, they had to add it as a 'One-time drink', resulting in meaningless data.

The solution (V2): A complete overhaul of the SQLite schema to support a dynamic amount of user-defined drinks. Whether its a morning coffee or once-a-year birthday wine, the app now scales with the user's life.
This requires a fundamental rewrite of the database architecture and a refresh of the UI to handle dynamic lists.

### Progress:
🔥 - Indicates feature not present in V1
- [x] View hydration status
- [x] Log a drink
- [ ] Day history quick-view (See drink count per drink on the Home screen)
- [x] Animate hydration UI (Text, liquid)
- [ ] Ability to disable animations 🔥
- [ ] View the days history (drink type, size, time), time plot🔥 and delete individual logs
- [ ] Add and customise drinks
- [ ] Update hydration goal
- [ ] Reset the days progress
- [ ] View the weeks history with day introspection and weekly statistics
- [ ] View the months history with day introspection and monthly statistics 🔥
- [ ] Enable/Disable a day from being included in statistics
- [ ] Long press drink log buttons to re-order OR add custom amount 🔥
- [ ] Setup hydrate reminder notifications at custom intervals 🔥
- [ ] Create homescreen widget 🔥
- [ ] Add sound effects 🔥
      

## Tech Stack
Language: Java  
Database: SQLite
