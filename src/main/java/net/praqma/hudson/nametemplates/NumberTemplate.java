package net.praqma.hudson.nametemplates;

        import hudson.FilePath;
        import net.praqma.hudson.CCUCMBuildAction;

        /**
         * The NumberTemplate class extends the Template class and provides
         * a method to parse a CCUCMBuildAction and return the build number as a String.
         */
        public class NumberTemplate extends Template {

            /**
             * Parses the given CCUCMBuildAction and returns the build number as a String.
             * If the action or the build is null, it returns "?".
             *
             * @param action the CCUCMBuildAction to parse
             * @param args additional arguments (not used in this implementation)
             * @param ws the FilePath workspace (not used in this implementation)
             * @return the build number as a String, or "?" if the action or build is null
             */
            @Override
            public String parse(CCUCMBuildAction action, String args, FilePath ws) {
                if (action == null || action.getBuild() == null) {
                    return "?";
                }
                return action.getBuild().getNumber() + "";
            }

        }
