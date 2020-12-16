export default class Workflow {
  static STEPS = {
    entry: 'ENTRY',
    cleansing: 'CLEANSING',
    analysis: 'ANALYSIS',
  }

  static STEP_CODES = Object.values(Workflow.STEPS)

  static STEP_NUMBER_BY_NAME = {
    [Workflow.STEPS.entry]: 1,
    [Workflow.STEPS.cleansing]: 2,
    [Workflow.STEPS.analysis]: 3,
  }

  static NEXT_STEP_BY_STEP = {
    [Workflow.STEPS.entry]: Workflow.STEPS.cleansing,
    [Workflow.STEPS.cleansing]: Workflow.STEPS.analysis,
    [Workflow.STEPS.analysis]: null,
  }

  static PREV_STEP_BY_STEP = {
    [Workflow.STEPS.entry]: null,
    [Workflow.STEPS.cleansing]: Workflow.STEPS.entry,
    [Workflow.STEPS.analysis]: Workflow.STEPS.cleansing,
  }

  static getStepNumber(step) {
    return Workflow.STEP_NUMBER_BY_NAME[step]
  }

  static getNextStep(step) {
    return Workflow.NEXT_STEP_BY_STEP[step]
  }

  static getPrevStep(step) {
    return Workflow.PREV_STEP_BY_STEP[step]
  }
}
