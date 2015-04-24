# MuseIOReceiver
School project to receive data from Muse headband

- Collects EEG data from FP1, TP9, TP10, FP2 (10-20 System)
- Uses FFT to extract the power in the theta, alpha, beta and gamma frequency bands
- Training data collected for a 'relax' phase and a 'focus' phase

- Currently using k-nn algorithm to classify new testing data into one of the 2 classes (relax, focus) using standardized Euclidean distance

- Implemented a 10-fold cross validation to determine the accuracy of the k-nn algorithm using different k values
  - Able to achieve ~90% accuracy with k values 5-15


- End Goal: Use the developed classification technique to control the mobile device
  - E.g. Playing a game using 'mind control'
  - E.g. Identifying periods of relaxation and concentration while user is performing some task on the mobile device
